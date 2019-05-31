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

    public WikiPhilosophy (Elements paragraphs) {
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
        // String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";
        String source = "https://en.wikipedia.org/wiki/Java";

        testConjecture(destination, source, 30);
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
            if (visited.contains(url)) {
                System.err.println("ERROR: Link has been visited before. in a loop. exiting");
                return; // using return because you cannon break from if statement
            } else {
                visited.add(url);
            }

            // get the paragraphs
            Element link = getFirstValidLink(url);
            if (link == null) {
                System.err.println("ERROR: No outgoing links found from "+url);
                return;
            }

            System.out.println(i + " *** "+link.text()+" ***");
            url = link.attr("abs:href");

            if (url.equals(destination)){
                System.out.println("SUCCESS: reached destination: " + destination + " from: " + source + " in " + i + " iterations.");
                break;
            }
        }
        if (!url.equals(destination) && i > limit) {
            System.err.println("ERROR: limit " + limit + " reached.");
            return;
        }
    }

     /**
	 * Formats and print the arguments.
	 * 
	 * @param msg
	 * @param args
	 */
    public static void print (String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

     /**
	 * Loads and parses a URL, then extracts the first link.
	 * Get paragraphs - construct WikiPhilosophy object - findFirstLink from given paragraphs
     * 
	 * @param url
	 * @return the Element of the first link, or null.
	 * @throws IOException
	 */
	public static Element getFirstValidLink(String url) throws IOException {
        System.out.println();
        print("Fetching %s...", url);
        Elements paragraphs = wf.fetchWikipedia(url); // selects the content text and returns the paragraphs.
        
        WikiPhilosophy wp = new WikiPhilosophy(paragraphs); // initializes paragraphs Elements and paranthesis stack
        Element elt = wp.findFirstLink(); // get first link
        return elt;
    }

    // This is where to implement FindFirstLinkPara
    /**
    * Returns the first valid link in a paragraph, or null.
    * Parse throu paragraphs - create Iterable for the first paragraph - get first link and validate
    * @param root
    */
    public Element findFirstLink() {
        // go through paragraphs

        for (Element paragraph : paragraphs) {
            // create iterable to traverse throu DOM tree
            // System.out.println(paragraphs);
            // System.out.println("******************************************");
            // System.out.println();
            // System.out.println(paragraph);
            
            Iterable<Node> itr = new WikiNodeIterable(paragraph);
            for (Node node : itr) { 
                
                // each node is either element ot textnode
                // if textnode -> need to check for paranthesis --> calls processTextNode
                // if element -> validate if its a link && correct link --> calls processElement
                if ((node instanceof TextNode)) {
                    // need to typeCast node to textNode
                    processTextNode((TextNode) node);
                }
                if (node instanceof Element) {
                    // need to typeCast node to Elemet
                    Element firstLink = processElement((Element) node);
                    if (firstLink != null) {
                        return firstLink;
                    }
                }
                // break;
            } 
        }
        return null;
    }

    public void processTextNode(TextNode node) {
        StringTokenizer st = new StringTokenizer(node.text(), " ()", true); 
        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            if (token.equals("(")) {
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
    public Element processElement(Element elt) {
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
    public boolean validLink(Element elt) {
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
        if (isInParanthesis(elt)) {
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
     * 
     */
    public boolean startsWith(Element elt, String s) {
        return (elt.attr("href").startsWith(s));
    }

    /**
     * Checks whether a link is italic.
     * 
     * @param start
     * 
     */
    public boolean isItalic(Element start) {
        for (Element elt=start; elt != null; elt = elt.parent()) {
            if (elt.tagName().equals("i") || elt.tagName().equals("em")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a link is in paranthesis.
     * 
     * @param elt
     * 
     */
    public boolean isInParanthesis(Element elt) {
        return (!parenthesisStack.isEmpty());
    }



}