// package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Deque;
import java.util.Scanner;


import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import java.util.StringTokenizer;


public class WikiPhilosophy {

    final static List<String> visited = new ArrayList<String>();
    final static WikiFetcher wf = new WikiFetcher();

    private Deque<String> parenthesisStack; // used check for paranthesis
	private Elements paragraphs; // the list of paragraphs we should search

    /**
     * Tests a conjecture about Wikipedia and Philosophy.
     *
     * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
     *
     * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String destination = "https://en.wikipedia.org/wiki/Philosophy";
        String source = "https://en.wikipedia.org/wiki/Java";

        testConjecture(destination, source, 10);
    }

    public WikiPhilosophy(Elements paragraphs) {
        this.paragraphs = paragraphs;
        this.parenthesisStack = new ArrayDeque<String>();
    }


    

    /**
     * Starts from given URL and follows first link until it finds the destination or exceeds the limit.
     *
     * @param destination
     * @param source
     * @throws IOException
     */
    public static void testConjecture(String destination, String source, int limit) throws IOException {
        // TODO: FILL THIS IN!
        int i=1;
        String url = source;
        Scanner kbd = new Scanner (System.in);
        boolean retry = true;

        while(retry) {
            for (; i<=limit; i++) {
                if (visited.contains(url)){
                    System.err.println("Error: loop");
                    return;
                } else {
                    visited.add(url);
                }

                Element link = getFirstValidLink(url);
                if (link == null) {
                    System.err.println("ERROR: no outgoing link");
                    return;
                }

                System.out.println("*** Iteration #"+ i +" *** "+link.text()+" ***");
                url = link.attr("abs:href");

                if (url.equals(destination)){
                    System.out.println("SUCCESS: reached destination: " + destination + " from: " + source + " in " + i + " iterations.");
                    retry = false;
                    break;
                }
            }

            if (i>limit) {
                System.err.println("ERROR: limit "+limit+" has been reached. Would you like to increase the limit to continue the program? (y/n)");
                String decision = kbd.nextLine();

                switch(decision)
                {
                    case "y":
                        retry = true;
                        System.out.println("You Answered Yes. Limit Will be Increased by 10");
                        limit += 10;
                        break;

                    case "n":
                        retry = false;
                        // break;
                        // retry = 0;
                        return;

                    default:
                        System.out.println("please enter again ");
                        break;
                }
                // return;
            }
        }
    }

    /**
     * Calls constructor for parahraphs array (ArrayLink) & visited array
     *
     * @param url
     * @return paragraphs
     * 
     */
    public static Element getFirstValidLink(String url) throws IOException{
        System.out.println();
        print("Fetching %s...", url);
        Elements paragraphs = wf.fetchWikipedia(url);

        WikiPhilosophy wp = new WikiPhilosophy(paragraphs);
        Element elt = wp.findFirstLink();
        return elt;
    }

    public Element findFirstLink() {
        for (Element paragraph : paragraphs) {
            Iterable<Node> iter = new WikiNodeIterable(paragraph);
            for (Node node : iter) {
                if (node instanceof TextNode) {
                    processTextNode((TextNode) node);
                }
                if (node instanceof Element) {
                    Element firstLink = processElement((Element) node);
                    if (firstLink != null) {
                        return firstLink;
                    }
                }
            }
        }
        return null;
    }


    public void processTextNode(TextNode node) {
        StringTokenizer st = new StringTokenizer(node.text(), " ()", true);
        while (st.hasMoreTokens()) {
            // node = node.parent();
            String token = st.nextToken();

            if (token.equals("(")) {
                parenthesisStack.add("(");
            }
            if (token.equals(")")) {
                if (parenthesisStack.isEmpty()) {
                    System.err.println("ERROR: Unbalanced Number of Paranthesis");
                }
                parenthesisStack.pop();
            }
        }
    }

    public Element processElement(Element elt) {
        if (validLink(elt)) {
            return elt;
        } 
        return null;
    }

    public boolean validLink(Element elt) {
        if (!elt.tagName().equals("a")) {
            return false;
        }
        if (isInParenthesis(elt)) {
            return false;
        }
        if (isItalic(elt)) {
            return false;
        }
        if (startsWith(elt, "#") || startsWith(elt, "/wiki/Help/")) {
            return false;
        }
        return true;
    }

    public boolean startsWith(Element elt, String str) {
        return (elt.attr("href").startsWith(str));
    }

    public boolean isItalic(Element elt) {
        for (Element e=elt; e!=null; e=e.parent()) {
            if (e.tagName().equals("em") || e.tagName().equals("i")) {
                return true;
            }
        }
        return false;
    }

    public boolean isInParenthesis(Element elt) {
        return (!parenthesisStack.isEmpty());
    }

    public static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

}