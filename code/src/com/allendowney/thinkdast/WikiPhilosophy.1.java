// package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Deque;


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
        String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";

        testConjecture(destination, source, 10);
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
        int i;
        String url = source;

        for (i=0; i<limit; i++) {
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

            System.out.println("*** "+ link.text()+" ***");
            url = link.attr("abs:href");

            if (url.equals(destination)){
                System.out.println("SUCCESS: reached destination: " + destination + " from: " + source + " in " + i + " iterations.");
                break;
            }
        }
        if (i==limit) {
            System.err.println("ERROR: limit "+limit+" has been reached.");
            return;
        }
    }

    /**
     * Calls constructor for parahraphs array (ArrayLink) & visited array
     *
     * @param url
     * @return paragraphs
     * 
     */
    public Element getFirstValidLink(String url) {

    }

}