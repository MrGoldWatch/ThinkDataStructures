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
	 * Initializes a WikiParser with a list of Elements.
	 * 
	 * @param paragraphs
	 */
    public WikiPhilosophy(Elements paragraphs) {
        this.paragraphs = paragraphs;
        this.parenthesisStack = new ArrayDeque<String>();
    }

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

        System.out.println();
        testConjecture(destination, source, 40);
    }

    /**
	 * Loads and parses a URL, then extracts the first link.
	 * 
	 * @param url
	 * @return the Element of the first link, or null.
	 * @throws IOException
	 */
	public static Element getFirstValidLink(String url) throws IOException {
        print("Fetching %s...", url);
        Elements paragraphs = wf.fetchWikipedia(url); // selects the content text and returns the paragraphs.

        // WikiParser wp = new WikiParser(paragraphs);
        WikiPhilosophy wp = new WikiPhilosophy(paragraphs);
		Element elt = wp.findFirstLink();
		return elt;
    }

    public Element findFirstLink() {
        for (Element paragraph : paragraphs) {
            // create an Iterable that traverses the tree
            Iterable<Node> itr = new WikiNodeIterable(paragraph); // creates a DOM tree 

            for (Node node: itr) { // traverse the DOM tree to find the first link
                if ((node instanceof TextNode)) {
                    // process TextNodes to get parentheses - need to typecast
                    processTextNode((TextNode) node); 
                }

                // check for tags <i> or <em> if element
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

    /**
	 * Processes a text node, splitting it up and checking parentheses.
	 * 
	 * @param node
	 */
	private void processTextNode(TextNode node) {
        // StringTokenizer(String str, String delim, boolean returnDelims)
        // Constructs a string tokenizer for the specified string.
        StringTokenizer st = new StringTokenizer(node.text(), " ()", true);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals("(")) {
                // System.out.println(token);
                parenthesisStack.push(token);
            }
            if (token.equals(")")) {
                if (parenthesisStack.isEmpty()) {
                    System.err.println("Warning: Unbalanced Number of Paranthesis");
                }
                parenthesisStack.pop();
            }
        }
    }

    /**
	 * Returns the element if it is a valid link, null otherwise.
	 * 
	 * 
	 * 
	 * @param elt
	 */
    private Element processElement(Element elt) {
        if (validLink(elt)){
            return elt;
        }
        return null;
    }
    
    /**
	 * Checks whether a link is value.
	 * 
	 * @param elt
	 * @return
	 */
    private boolean validLink(Element elt) {
        // check if its a link tag
        // check if its italic
        // check if its in paranthesis
        // check if its not external
        // check if its not the link to current page
        // check if its not help page

        if (!elt.tagName().equals("a")) {
            return false;
        }
        if (isItalic(elt)) {
            return false;
        }
        if (isInParens(elt)) {
            return false;
        }
        if (startsWith(elt, "#") || startsWith(elt, "/wiki/help/")) {
            return false;
        }
        return true;
    }
    
    /**
	 * Checks whether a link starts with a given String.
	 * 
	 * @param elt
	 * @param s
	 * @return
	 */
	private boolean startsWith(Element elt, String s) {
        return (elt.attr("href").startsWith(s));
    }

    /**
	 * Checks whether the element is in parentheses (possibly nested).
	 * 
	 * @param elt
	 * @return
	 */
    private boolean isInParens(Element elt) {
        return (!parenthesisStack.isEmpty());
    }
    
    /**
	 * Checks whether the element is in italics.
	 * 
	 * (Either a "i" or "em" tag)
	 * 
	 * @param start
	 * @return
	 */
    private boolean isItalic(Element start) {
        for (Element elt=start; elt != null; elt = elt.parent()) {
            if (elt.tagName().equals("i") || elt.tagName().equals("em")) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
	 * Formats and print the arguments.
	 * 
	 * @param msg
	 * @param args
	 */
    public static void print (String msg, Object... args) {
        System.out.println(String.format(msg, args));
        // System.out.println();
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
        // need to check the limit before anything
        String url = source;
        int i;
        for (i=0; i<limit; i++) {
            if (visited.contains(url)){
                System.err.println("ERROR: Encountered a visited link, were in a loop - exiting");
                return;
            } else {
                visited.add(url);
            }
            Element elt = getFirstValidLink(url);
            if (elt == null) {
                System.err.println("ERROR: No outgoing links found from "+url);
                return;
            }

            System.out.println("**"+ elt.text() +"**");
            url = elt.attr("abs:href");

            if (url.equals(destination)) {
                System.out.println("VOILA!!!! URL: "+url+" have been reached!!!");
                break;
            }
        }
        if (!url.equals(destination) && i == limit) {
            System.out.println("Error: limit has been reached.");
        }
    }
}
