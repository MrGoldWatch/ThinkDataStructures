// package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Represents a Redis-backed web search index.
 *
 */
public class JedisIndex {

	private Jedis jedis;

	/**
	 * Constructor.
	 *
	 * @param jedis
	 */
	public JedisIndex(Jedis jedis) {
		this.jedis = jedis;
	}

	/**
	 * Returns the Redis key for a given search term.
	 *
	 * @return Redis key.
	 */
	private String urlSetKey(String term) {
		return "URLSet:" + term;
	}

	/**
	 * Returns the Redis key for a URL's TermCounter.
	 *
	 * @return Redis key.
	 */
	private String termCounterKey(String url) {
		return "TermCounter:" + url;
	}

	/**
	 * Checks whether we have a TermCounter for a given URL.
	 *
	 * @param url
	 * @return
	 */
	public boolean isIndexed(String url) {
		String redisKey = termCounterKey(url);
		return jedis.exists(redisKey);
	}
	
	/**
	 * Adds a URL to the set associated with `term`.
	 * 
	 * @param term
	 * @param tc
	 */
	public void add(String term, TermCounter tc) {
		jedis.sadd(urlSetKey(term), tc.getLabel());
	}

	/**
	 * Looks up a search term and returns a set of URLs.
	 * 
	 * @param term
	 * @return Set of URLs.
	 */
	public Set<String> getURLs(String term) {
		Set<String> urls = jedis.smembers(urlSetKey(term));
		return urls;
	}

    /**
	 * Looks up a term and returns a map from URL to count.
	 * 
	 * @param term
	 * @return Map from URL to count.
	 */
	public Map<String, Integer> getCounts(String term) {
        // FILL THIS IN!
		// return null;
		Map<String, Integer> map = new HashMap<String, Integer>();
		Set<String> urls = getURLs(term); // now we have the URLs that contain the search term
		System.out.println(urls);
		for (String url : urls) {
			System.out.println("url is: "+url);
			System.out.println(getCount(url, term));
			map.put(url, getCount(url, term));
		}
		return map;
	}

    /**
	 * Returns the number of times the given term appears at the given URL.
	 * 
	 * @param url
	 * @param term
	 * @return
	 */
	public Integer getCount(String url, String term) {
        // FILL THIS IN!
		// return null;
		String count = jedis.hget(termCounterKey(url),term);
		// Integer count = s.toInteger();
		return Integer.parseInt(count);
	}

	/**
	 * Adds a page to the index.
	 *
	 * @param url         URL of the page.
	 * @param paragraphs  Collection of elements that should be indexed.
	 */
	public void indexPage(String url, Elements paragraphs) {
		// TODO: FILL THIS IN!
		// at this point we have the paragraphs to be parsed
		// we have the url to set our key for the URLSet Hash
		// going to utilize TermCounter class for parsing paragaraphs
		System.out.println("Indexing: "+url+" ...");

		// This will Encapsulate a map from search term to frequency (count).
		TermCounter tc = new TermCounter(url.toString());
		tc.processElements(paragraphs); // at this point we have the page indexed

		// need to push to redis set (set up URLSet)
		// need to push to redis hash (set up TermCounter) --> basically already done - just needs a method to push to jedis

		pushTermCounterToRedis(tc);

		Map<String, String> map = pullFromRedis(url.toString());
		for (Map.Entry<String, String> entry: map.entrySet()) {
			System.out.println(entry.getKey() + ", " + entry.getValue());
		}


	}

	public Map<String, String> pullFromRedis(String url) {
		//  = tc.getLabel();
		Map<String, String> result = jedis.hgetAll(termCounterKey(url));
		return result;
	}

	/**
	 * Pushes the contents of the TermCounter to Redis.
	 * 
	 */
	public List<Object> pushTermCounterToRedis(TermCounter tc) {
		Transaction t = jedis.multi();
		
		String url = tc.getLabel();
		String hashname = termCounterKey(url); // will return TermCounter: + url;
		t.del(hashname);

		for (String term: tc.keySet()) {
			Integer count = tc.get(term);
			t.hset(hashname, term, count.toString()); // add to Redis hash TermCounter
			t.sadd(urlSetKey(term), url); // add to Redis Set URLSet
		}
		List<Object> res = t.exec();
		return res;
	}
	/**
	 * Prints the contents of the index.
	 *
	 * Should be used for development and testing, not production.
	 */
	public void printIndex() {
		// loop through the search terms
		for (String term: termSet()) {
			System.out.println(term);

			// for each term, print the pages where it appears
			Set<String> urls = getURLs(term);
			for (String url: urls) {
				Integer count = getCount(url, term);
				System.out.println("    " + url + " " + count);
			}
		}
	}

	/**
	 * Returns the set of terms that have been indexed.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public Set<String> termSet() {
		Set<String> keys = urlSetKeys();
		Set<String> terms = new HashSet<String>();
		for (String key: keys) {
			String[] array = key.split(":");
			if (array.length < 2) {
				terms.add("");
			} else {
				terms.add(array[1]);
			}
		}
		return terms;
	}

	/**
	 * Returns URLSet keys for the terms that have been indexed.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public Set<String> urlSetKeys() {
		return jedis.keys("URLSet:*");
	}

	/**
	 * Returns TermCounter keys for the URLS that have been indexed.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public Set<String> termCounterKeys() {
		return jedis.keys("TermCounter:*");
	}

	/**
	 * Deletes all URLSet objects from the database.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public void deleteURLSets() {
		Set<String> keys = urlSetKeys();
		Transaction t = jedis.multi();
		for (String key: keys) {
			t.del(key);
		}
		t.exec();
	}

	/**
	 * Deletes all URLSet objects from the database.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public void deleteTermCounters() {
		Set<String> keys = termCounterKeys();
		Transaction t = jedis.multi();
		for (String key: keys) {
			t.del(key);
		}
		t.exec();
	}

	/**
	 * Deletes all keys from the database.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public void deleteAllKeys() {
		Set<String> keys = jedis.keys("*");
		Transaction t = jedis.multi();
		for (String key: keys) {
			t.del(key);
		}
		t.exec();
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis);
		String url;

		index.deleteTermCounters();
		index.deleteURLSets();
		index.deleteAllKeys();
		loadIndex(index);


		Set<String> urlKeys = index.termCounterKeys();
		for (String s : urlKeys) {
			System.out.println(s);
		}

		// Set<String> keys = urlSetKeys();
		// System.out.println(termCounterKeys());	
		System.out.println("");
		// Set<String> set = index.getURLs("URLSet:normally");
		// System.out.println(set);
		// for (String s : set) {
			// System.out.println(s);

			// System.out.println(getCount(s,"normally"));
			// url = s;
		// }

		System.out.println(index.getCount("https://en.wikipedia.org/wiki/Java","growth"));
		System.out.println("calling getCounts");
		Map<String, Integer> maps = index.getCounts("growth");
		for (Entry<String, Integer> entry: maps.entrySet()) {
			System.out.println(entry);
		}
	}

	/**
	 * Stores two pages in the index for testing purposes.
	 *
	 * @return
	 * @throws IOException
	 */
	private static void loadIndex(JedisIndex index) throws IOException {
		WikiFetcher wf = new WikiFetcher();

		String url = "https://en.wikipedia.org/wiki/Java";
		// Elements paragraphs = wf.readWikipedia(url);
		Elements paragraphs = wf.fetchWikipedia(url);
		index.indexPage(url, paragraphs);

		

		// url = "https://en.wikipedia.org/wiki/Programming_language";
		// paragraphs = wf.readWikipedia(url);
		// index.indexPage(url, paragraphs);
	}
}
