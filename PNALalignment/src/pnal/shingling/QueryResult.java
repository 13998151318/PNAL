package pnal.shingling;

/**
 * This class is part of the PARIS ontology matching project at INRIA
 * Saclay/France.
 * 
 * It is licensed under a Creative Commons Attribution Non-Commercial License

 * by the author Mayur Garg. For all further information, see

 * http://webdam.inria.fr/paris

 */

import java.io.Serializable;

public class QueryResult implements Serializable {
	private static final long serialVersionUID = -418561007747745856L;
	public String result;
	public double estimatedScore;
	public double trueScore;

	public QueryResult(String s, double eS, double tS) {
		result = s;
		estimatedScore = eS;
		trueScore = tS;
	}
}
