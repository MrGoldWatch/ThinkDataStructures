// package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Attributes;

import packages.*;

public class WikiNodeExample {
	
	public static void main(String[] args) throws IOException {
        String url = "https://en.wikipedia.org/wiki/Java";
        System.out.println();
		
		// download and parse the document
		Connection conn = Jsoup.connect(url);
		Document doc = conn.get();
		
		// select the content text and pull out the paragraphs.
		Element content = doc.getElementById("mw-content-text");
				
		// TODO: avoid selecting paragraphs from sidebars and boxouts
        Elements paras = content.select("p");
        // System.out.println(paras);

        Element firstPara = paras.get(0);
        // System.out.println(firstPara);
        
        // To get the first element which has tag <a>
        Elements links = paras.select("a");
        Element firstLink = links.get(0);
        
        // Cant do this becuase of the link restrictions
        // Can be replaced by the attr(key) method from Node class inside the below for loop
        // To get the value of the href attribute
        Attributes attrb = firstLink.attributes();
        String hrefAttrb = attrb.get("href");
        // System.out.println(firstLink); // prints the first accurance of <a> tag
        // System.out.println(attrb); // prints the attributes of the first <a> tag
        // System.out.println(hrefAttrb); // prints the value for attribute key "href"
        
        
        // do i need to any DFS??
        // recursiveDFS(firstPara);
		System.out.println();

		// iterativeDFS(firstPara);
		System.out.println();

        // why do i need this? 
        // to use class Node's attr(key) method
        Iterable<Node> iter = new WikiNodeIterable(firstPara);
        System.out.print(iter);
		for (Node node: iter) {

            // System.out.println();

            // String nodeAttrb = node.attr("href");
            // System.out.println(nodeAttrb);
            // System.out.println();
			if ((node instanceof TextNode)) {
				// System.out.print(node);
            }  
            if (node instanceof Element) {
                // System.out.println(node);
            }
		}
	}

	private static void iterativeDFS(Node root) {
		Deque<Node> stack = new ArrayDeque<Node>();
		stack.push(root);

		// if the stack is empty, we're done
		while (!stack.isEmpty()) {

			// otherwise pop the next Node off the stack
			Node node = stack.pop();
			if (node instanceof TextNode) {
				System.out.print(node);
			}

			// push the children onto the stack in reverse order
			List<Node> nodes = new ArrayList<Node>(node.childNodes());
			Collections.reverse(nodes);
			
			for (Node child: nodes) {
				stack.push(child);
			}
		}
	}

	private static void recursiveDFS(Node node) {
		if (node instanceof TextNode) {
			System.out.print(node);
		}
		for (Node child: node.childNodes()) {
			recursiveDFS(child);
		}
	}
}
