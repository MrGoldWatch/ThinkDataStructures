// package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Deque;
import java.util.Iterator;

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

        testConjecture(destination, source, 30);
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
        int i;
        String url = source;

        for (i=1; i<=limit; i++) {
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
                break;
            }
        }
        if (i>limit) {
            System.err.println("ERROR: limit "+limit+" has been reached.");
            return;
        }
    }

    /**
     * Calls WikiFetcher which returns paragraph elements then
     * Calls constructor for parahraphs array (ArrayLink) & parenthesisStack
     * For each paragraph in paragraphs it iterizes the elements
     * Calls elementProcess(check for valid link) or textProcess (check for paranthesis)
     *
     * @param url
     * @return paragraphs
     * 
     */
    public static Element getFirstValidLink(String url) throws IOException {
        System.out.println();
        // System.out.println("Fetching "+url);
        print("Fetching %s...",url);
        Elements paragraphs = wf.fetchWikipedia(url);

        WikiPhilosophy wp = new WikiPhilosophy(paragraphs);
        Element elt = wp.findFirstLink();
        return elt;
    }

    // This is where to implement FindFirstLinkPara
    /**
    * Returns the first valid link in a paragraph, or null.
    * Parse throu paragraphs - create Iterable for the first paragraph - get first link and validate
    * @param root
    */
    public Element findFirstLink() {
        for (Element paragraph : paragraphs) {
            Iterable<Node> nodes = new WikiNodeIterable(paragraph);

            for (Node node : nodes) {
                if (node instanceof TextNode) {
                    processTextNode((TextNode) node);
                }
                if (node instanceof Element) {
                    Element firstLink = processElement((Element) node);
                    if (firstLink != null) {
                        // System.err.println("ERROR: No Outgoing urls");
                        // return null;
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
        if (validLink(elt)){
            return elt;
        }
        return null;
    }

    public boolean validLink(Element elt) {
        // starts with # or /wiki/help
        // is italics
        // is in paranthesis
        // check if its a link tag

        // if (elt.attr('href').)
        if (!elt.tagName().equals("a")) {
            return false;
        }
        if (isInParanthesis(elt)) {
            return false;
        }
        if (isIalics(elt)) {
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

    public boolean isInParanthesis(Element elt) {
        return (!parenthesisStack.isEmpty());
    }

    public boolean isIalics(Element start) {
        for (Element elt=start; elt!=null; elt=elt.parent()) {
            if (elt.tagName().equals("em") || elt.tagName().equals("i")) {
                return true;
            }
        }
        return false;
    }

    public static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

}