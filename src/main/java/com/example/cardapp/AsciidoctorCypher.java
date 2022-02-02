package com.example.cardapp;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.StructuralNode;
import org.neo4j.driver.Driver;

import java.nio.file.Path;

public class AsciidoctorCypher {

	public static void executeDocument(Driver driver, Path documentPath) {
		var asciidoctor = Asciidoctor.Factory.create();
		var document = asciidoctor.loadFile(documentPath.toFile(), Options.builder().build());
		handle(driver, document);
	}

	private static void handle(Driver driver, StructuralNode document) {
		for (StructuralNode block : document.getBlocks()) {
			handleDocumentNode(driver, block);
			handle(driver, block);
		}
	}

	private static void handleDocumentNode(Driver driver, StructuralNode documentNode) {
		var language = documentNode.getAttribute("language");
		if (documentNode.isBlock() && documentNode.getContext().contains("listing") && language != null && language.equals("cypher")) {
			executeBlock(driver, (Block) documentNode);
		}
	}

	private static void executeBlock(Driver driver, Block blockBlock) {
		try (var session = driver.session()) {
			var source = blockBlock.getSource();
			session.run(source).consume();
		}
	}

}
