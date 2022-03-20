/**
 * 
 */
package edu.wisc.cs.will.ILP;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.wisc.cs.will.DataSetUtils.Example;
import edu.wisc.cs.will.FOPC.Clause;
import edu.wisc.cs.will.FOPC.HandleFOPCstrings;
import edu.wisc.cs.will.FOPC.Literal;
import edu.wisc.cs.will.FOPC.LiteralToThreshold;
import edu.wisc.cs.will.FOPC.NumericConstant;
import edu.wisc.cs.will.FOPC.PredicateName;
import edu.wisc.cs.will.FOPC.PredicateNameAndArity;
import edu.wisc.cs.will.FOPC.RelevanceStrength;
import edu.wisc.cs.will.FOPC.Sentence;
import edu.wisc.cs.will.FOPC.Term;
import edu.wisc.cs.will.FOPC.TypeSpec;
import edu.wisc.cs.will.FOPC.Unifier;
import edu.wisc.cs.will.FOPC.Variable;
import edu.wisc.cs.will.Utils.MessageType;
import edu.wisc.cs.will.Utils.Utils;
import edu.wisc.cs.will.Utils.condor.CondorFileOutputStream;
import edu.wisc.cs.will.stdAIsearch.SearchInterrupted;

/**
 * Note: ALL proofs are found of literals being thresholded, and this could produce a huge list.
 * 
 * @author shavlik
 *
 */
public class ThresholdManager {
	protected final static int debugLevel = 0; // Used to control output from this project (0 = no output, 1=some, 2=much, 3=all).
	
	private LearnOneClause    innerLoopForILP; // Really only need this one, but get the others for convenience.
	private HandleFOPCstrings stringHandler;
	private Unifier           unifier;
	private InlineManager     inlineManager;
	
	private int lowerCounter = 0; // Need unique variables for each use.
	private int upperCounter = 0; // These three probably follow in lock-step, but down the road we might want them to do different things, so keep three counters.
	private int varCounter   = 0; 
	private int valueCounter = 0; 
	
	public boolean fullyTrustFirstArgs = true; // If this is true and firstArgIsExampleID is as well, then ignore values that do not connect to neither a positive nor a negative example. 
	
	/**
	 * 
	 */
	public ThresholdManager(LearnOneClause innerLoopForILP, HandleFOPCstrings stringHandler, Unifier unifier, InlineManager inlineManager) {
		this.innerLoopForILP = innerLoopForILP;
		this.stringHandler   = stringHandler;
		this.unifier         = unifier;
		this.inlineManager   = inlineManager;
	}
	
	public void processThresholdRequests(String fileName, Collection<LiteralToThreshold> literalsToThreshold) throws SearchInterrupted {
		try {
			CondorFileOutputStream outStream = (fileName == null ? null : new CondorFileOutputStream(fileName));
			PrintStream          printStream = (fileName == null ? null : new PrintStream(outStream, false)); // (Don't) Request auto-flush (can slow down code).
			if (printStream != null) printStream.println("%%%%%%%%%%%  DO NOT EDIT THIS FILE.  IT'LL BE RE-LOADED AS IS.  %%%%%%%%%%%");
			if (literalsToThreshold != null) for(LiteralToThreshold literal : literalsToThreshold) {
				createThresholds(printStream, stringHandler, literal, null, null);
			}
		}
		catch (FileNotFoundException e) {
			Utils.reportStackTrace(e);
			Utils.error("Unable to successfully open this file for writing: " + fileName + ".  Error message: " + e.getMessage());
		}
	}		

	// Given the list of values for this numeric argument (in position 'position,' counting from 1), create some literals specifying thresholds.
	// TODO: If TWO lists of values are provided, then thresholds will only be created between 'adjacent' values from
	//       different lists (as is done in decision-tree induction).  E.g., if one list is {1, 3, 5} and the other is {4, 6, 8, 10} then thresholds of interest are  3.5, 4.5, and 5.5.
	private void createThresholds(PrintStream printStream, HandleFOPCstrings stringHandler, LiteralToThreshold literalToThreshold, List<Double> class1values, List<Double> class2values) throws SearchInterrupted {
		long start = (debugLevel > -10 ? System.currentTimeMillis() : 0);
		if (debugLevel > -10) {
			Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "\n% Create a threshold for '" + literalToThreshold + "' using the value at arg #" + literalToThreshold.positionToThreshold + ".");
			if (printStream != null) printStream.println("\n% Create a threshold for '" + literalToThreshold + "' using the value at arg #" + literalToThreshold.positionToThreshold + ".");
			if (debugLevel > -1 && (Utils.getSizeSafely(class1values) + Utils.getSizeSafely(class2values) > 0)) {
				Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%  There currently are "  + (class1values == null ? "no"   : class1values.size()) +
							  " values for class 1 and " + (class2values == null ? "none" : class2values.size()) + " for class 2.");
			}
		}

		int     maxCuts             = literalToThreshold.maxCuts;
		Boolean createTiles         = literalToThreshold.createTiles;
		Boolean firstArgIsExampleID = literalToThreshold.firstArgIsExampleID;
		
		if (literalToThreshold.numberArgs() < 1) { return; }
		
		// No thresholds have been provided, so need to collect them.
		if (class1values == null && class2values == null) {
			Set<Term> posExIDs = null;
			Set<Term> negExIDs = null;
			
			class1values = new ArrayList<Double>(8);
			class2values = new ArrayList<Double>(8);
			if (firstArgIsExampleID) {
				List<Example> posExamples = innerLoopForILP.getPosExamples();
				List<Example> negExamples = innerLoopForILP.getNegExamples();

				posExIDs = new HashSet<Term>(4);
				negExIDs = new HashSet<Term>(4);
				for (Example pos : posExamples) { posExIDs.add(pos.getArgument(0)); }
				for (Example neg : negExamples) { negExIDs.add(neg.getArgument(0)); }
			}
		
			List<Term> argList = new ArrayList<Term>(literalToThreshold.numberArgs());
			for (int i = 0; i < literalToThreshold.numberArgs(); i++) {
				argList.add(stringHandler.getNewUnamedVariable());
			}
			
			Literal    query         = stringHandler.getLiteral(literalToThreshold.predicateName, argList);			
			Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%   query = " + query);
			
			List<Term> groundings    = innerLoopForILP.getProver().getAllUniqueGroundings(query, innerLoopForILP.getParser()); // Note: this can be very cpu intensive!
			Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%   |groundings| = " + Utils.getSizeSafely(groundings));			
			if (groundings == null) { return; }
			
			int rejectCounter   = 0;
			int countAddedBoth  = 0;
			int countAdded1only = 0;
			int countAdded2only = 0;
			for (Term factAsTerm : groundings) {		
				Literal fact = factAsTerm.asLiteral();
				int numbArgs = fact.numberArgs();
				int wArg     = stringHandler.getArgumentPosition(stringHandler.locationOfWorldArg, numbArgs);
				int sArg     = stringHandler.getArgumentPosition(stringHandler.locationOfStateArg, numbArgs);	
				Term argToThreshold = fact.getArgument(literalToThreshold.positionToThreshold - 1);  // Remember that counting starts from 1.
				Term firstArg       = (firstArgIsExampleID ? fact.getArgument(0) : null);
				
				// firstArgIsExampleID overrides innerLoopForILP.whenComputingThresholdsWorldAndStateArgsMustBeWorldAndStateOfAcurrentExample
				if (!firstArgIsExampleID && innerLoopForILP.whenComputingThresholdsWorldAndStateArgsMustBeWorldAndStateOfAcurrentExample) {					
					if (wArg >= 0 && wArg < numbArgs && sArg >= 0 && sArg < numbArgs) {
						if (!innerLoopForILP.isWorldStateArgPairInAnExample(fact.getArgument(wArg), fact.getArgument(sArg))) { 
							if (debugLevel > -1) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "% Ignore [#" + Utils.comma(++rejectCounter) + "] '" + fact + "' because doesn't match a training example."); }
							continue; 
						}
					}
				}
				
				// Utils.println("%   fact = " + fact + "  argToThreshold = " + argToThreshold);				
				if (argToThreshold instanceof NumericConstant) {
					double valueInFact = ((NumericConstant) argToThreshold).value.doubleValue();
					if (debugLevel > 1) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%   found: " + valueInFact + " in '" + fact + "'"); }
					boolean inPos = (firstArgIsExampleID && posExIDs.contains(firstArg)) || (!firstArgIsExampleID && innerLoopForILP.whenComputingThresholdsWorldAndStateArgsMustBeWorldAndStateOfAcurrentExample && innerLoopForILP.isWorldStateArgPairInAnPosExample(fact.getArgument(wArg), fact.getArgument(sArg)));
					boolean inNeg = (firstArgIsExampleID && negExIDs.contains(firstArg)) || (!firstArgIsExampleID && innerLoopForILP.whenComputingThresholdsWorldAndStateArgsMustBeWorldAndStateOfAcurrentExample && innerLoopForILP.isWorldStateArgPairInAnNegExample(fact.getArgument(wArg), fact.getArgument(sArg)));
					if (inPos && inNeg) {
						class1values.add(valueInFact); // If ambiguous, play it safe.
						class2values.add(valueInFact);
						countAddedBoth++;
					} else if (inPos) {
						class1values.add(valueInFact); 
						countAdded1only++;
					} else if (inNeg) {
						class2values.add(valueInFact); 
						countAdded2only++;
					} else if (!fullyTrustFirstArgs && firstArgIsExampleID) { // If looking for first arg's but didn't find it, play it safe and put in both (arguably could put in NEITHER, if we fully trusted things - i.ee. this could be a spurious fact not involved in these examples.
						class1values.add(valueInFact);
						class2values.add(valueInFact);
						countAddedBoth++;
					} else {
						class1values.add(valueInFact);
						countAdded1only++;
					}
				}
				else {
					Utils.warning("% Was expecting a number in argument #" + literalToThreshold.positionToThreshold + " but got: '" + argToThreshold + "' in '" + fact + "'.  Will ignore this fact."); 
				}
			}
			if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "% rejectCount = " + Utils.comma(rejectCounter) + " countAddedBoth = "  + Utils.comma(countAddedBoth) + " countAdded1only = " + Utils.comma(countAdded1only) + " countAdded2only = " + Utils.comma(countAdded2only)); }
		}
		
		
		List<Double> orgList1 = (class1values == null ? null : new ArrayList<Double>(class1values.size()));
		List<Double> orgList2 = (class2values == null ? null : new ArrayList<Double>(class2values.size()));
		if (class1values != null) { for (Double d : class1values) orgList1.add(d); }
		if (class2values != null) { for (Double d : class2values) orgList2.add(d); }
		
		if (class1values != null) { Utils.sortListOfDoublesAndRemoveDuplicates(class1values); } // Sorting is done in place.
		if (class2values != null) { Utils.sortListOfDoublesAndRemoveDuplicates(class2values); } // Sorting is done in place.
		if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%  After sorting and removing duplicates, there now are " + (class1values == null ? -1 : class1values.size()) + " values for class 1 and " + (class2values == null ? -1 : class2values.size()) + " for class 2."); }
		if (debugLevel > 1 && Utils.getSizeSafely(class1values) > 0) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "    class1: " + class1values); }
		if (debugLevel > 1 && Utils.getSizeSafely(class2values) > 0) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "    class2: " + class2values); }
		if (debugLevel > 1 && Utils.getSizeSafely(class1values) > 0) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "    orgList1: " + orgList1); }
		if (debugLevel > 1 && Utils.getSizeSafely(class2values) > 0) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "    orgList2: " + orgList2); }
		
		// In the next group, we reduce the TWO sets of values into ONE.
		int numbClass1values = Utils.getSizeSafely(class1values);
		int numbClass2values = Utils.getSizeSafely(class2values);
		if (numbClass1values < 1 && numbClass2values > 0) { // In only have class2 values, assign all values to class1.
			class1values = class2values;
			class2values = null;			
		} else if (numbClass1values > 0 && numbClass2values > 0) { // If we have both class1 and class2 values, need to collect the boundaries.
			List<Double> valuesToKeep = new ArrayList<Double>(8);
			Double old1 = null;
			Double old2 = null;
			int counter1 = 0;
			int counter2 = 0;
			double frontOf1 = class1values.get(counter1);
			double frontOf2 = class2values.get(counter2);
			int    focus    = (frontOf1 < frontOf2 ? 1 : 2); // Keep track of which list has the smaller number.
			
			while (counter1 < numbClass1values && counter2 < numbClass2values) {
				frontOf1 = class1values.get(counter1);
				frontOf2 = class2values.get(counter2);
				
				if (debugLevel > 1) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%  focus = " + focus + " front1 = " + frontOf1 + "  front2 = " + frontOf2 + "  old1 = " + old1 + "  old2 = " + old2); }
				if (frontOf1 == frontOf2) {
					if (!valuesToKeep.contains(frontOf1))                               { valuesToKeep.add(frontOf1); }
					if (focus == 1) { if (old1 != null && !valuesToKeep.contains(old1)) { valuesToKeep.add(old1); } }
					if (focus == 2) { if (old2 != null && !valuesToKeep.contains(old2)) { valuesToKeep.add(old2); } }
					counter1++;
					counter2++;	
					old1 = frontOf1;
					old2 = frontOf2;
					if (debugLevel > 1) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%  TIE, add tying number " + frontOf1 + " and previous number " + (focus == 1 ? old1 : old2)); }
				} else if (focus == 1 && frontOf1 < frontOf2) {
					old1  = frontOf1;
					counter1++;  // Keep advancing.
				} else if (focus == 2 && frontOf2 < frontOf1) {
					old2  = frontOf2;
					counter2++;  // Keep advancing.
				} else if (frontOf1 < frontOf2) {
					if (old2 != null && old2 <= frontOf1) { // Have crossed a 'transition' in the two lists.
						if (!valuesToKeep.contains(old2))     { valuesToKeep.add(old2);     }
						if (!valuesToKeep.contains(frontOf1)) { valuesToKeep.add(frontOf1); }
						if (debugLevel > 1) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%   crossed 1:2 transistion, adding " + old2 + " and " + frontOf1); }
					}
					counter1++; // List 1 is now the smaller.
					old1  = frontOf1;	
					focus = 1;
				} else { // frontOf1 > frontOf2   NOTE: dont need -inf and + inf for first numbers?
					if (old1 != null && old1 <= frontOf2) { // Have crossed a 'transition' in the two lists.
						if (!valuesToKeep.contains(old1))     { valuesToKeep.add(old1);     }
						if (!valuesToKeep.contains(frontOf2)) { valuesToKeep.add(frontOf2); }
						if (debugLevel > 1) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%    crossed 2:1 transistion, adding " + old1 + " and " + frontOf2); }
					}
					counter2++; // List 2 is now the smaller.
					old2  = frontOf2;
					focus = 2;
				}				
			}
			// Need to handle any remaining numbers on one list or the other.
			if (counter1 <  numbClass1values) { if (!valuesToKeep.contains(frontOf1)) { valuesToKeep.add(frontOf1); }}
			if (counter2 <  numbClass2values) { if (!valuesToKeep.contains(frontOf2)) { valuesToKeep.add(frontOf2); }}
			if (counter1 >= numbClass1values) { if (!valuesToKeep.contains(old1))     { valuesToKeep.add(old1); }}
			if (counter2 >= numbClass2values) { if (!valuesToKeep.contains(old2))     { valuesToKeep.add(old2); }}
			
			// Do a final sorting rather than having the above lines check for various cases.			
			class1values = valuesToKeep;
			class2values = null;
			
			if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%  After merging the two lists, there now are " + Utils.comma(class1values == null ? -1 : class1values.size()) + " values for class 1 and " + (class2values == null ? "none" : class2values.size()) + " for class 2."); }
			if (debugLevel > 1 && Utils.getSizeSafely(class1values) > 0) { Utils.println("%    class1: " + class1values); }	
			
			if (Utils.getSizeSafely(class1values) > (createTiles ? 550 : 110000)) { // If we have a lot of candidates, define a (potentially) larger than usual tolerance.
				double min = Double.POSITIVE_INFINITY;
				double max = Double.NEGATIVE_INFINITY;
				for (Double dbl : class1values) {
					if (dbl < min) { min = dbl; }
					if (dbl > max) { max = dbl; }
				}
				double thresholdForEquivalence = Math.max(0.000001, (max - min) / 10000);
				double thresholdForNearZero    = Math.max(8 * Double.MIN_NORMAL, thresholdForEquivalence / 10);
				if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%    Using thresholds of " + thresholdForEquivalence + " and " + thresholdForNearZero); }
				Utils.sortListOfDoublesAndRemoveDuplicates(valuesToKeep, thresholdForEquivalence, thresholdForNearZero); // Sorting is done in place.
			} else {
				Utils.sortListOfDoublesAndRemoveDuplicates(valuesToKeep); // Sorting is done in place.
			}
			if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%  After reducing duplicates (equal within a threshold), there now are " + Utils.comma(class1values == null ? -1 : class1values.size()) + " values for class 1 and " + (class2values == null ? "none" : class2values.size()) + " for class 2."); }
			
			
			if (Utils.getSizeSafely(class1values) > (createTiles ? 550 : 110000)) { // Will reduce to 500 (if creating tiles; 500^2 = 250000, but we only use roughly half since we need lower < upper), but don't bother if 'close'.
				if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%  Will randomly reduce the number of class1 values to about 500."); }
				List<Double> newClass1values = new ArrayList<Double>(500);
				double prob = (createTiles ? 500.0 : 100000.0) / Utils.getSizeSafely(class1values);
				for (Double dbl : class1values) if (Utils.random() <= prob) { newClass1values.add(dbl); }
				class1values = newClass1values;
				if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%  Now have " + Utils.comma(newClass1values) + " class1 values."); }
			}
		} 
		
		int remainingThresholds = Utils.getSizeSafely(class1values);
		remainingThresholds    += Utils.getSizeSafely(class2values);		
		if  (remainingThresholds < 1) {
			if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%  Since no remaining thresholds, will NOT create any thresholded predicates."); }
			return;
		}
		
		if (Utils.getSizeSafely(class1values) > 0 && Utils.getSizeSafely(class2values) > 0) {
			Utils.error("Something went wrong.  Have |class1values| = " + Utils.comma(class1values) + " and |class2values| = " + Utils.comma(class2values) + ".");
		}
		else if (Utils.getSizeSafely(class1values) +  Utils.getSizeSafely(class2values) > 1) {	// Need more than one value to make thresholds. 			
			// Create a mode for the new predicate.
	   		List<Term> extendedArguments = new ArrayList<Term>(literalToThreshold.numberArgs());
	   		extendedArguments.addAll(literalToThreshold.getArguments()); // Need NEW list.
	        
	   		Term     termThresholded = literalToThreshold.getArguments().get(literalToThreshold.positionToThreshold - 1);
	   		TypeSpec spec            = termThresholded.getTypeSpec();
	   		Term     newTypedTerm    = stringHandler.getAnonymousTerm(new TypeSpec("#", spec.isaType.typeName, stringHandler));
	   		extendedArguments.add(newTypedTerm); // The lower bound.
	   		extendedArguments.add(newTypedTerm); // And the upper.
	   		PredicateName newPname    = stringHandler.getPredicateName(literalToThreshold.predicateName + "_WILL_temporaryInBetween");
	   		Literal       extendedLit = stringHandler.getLiteral(newPname, extendedArguments);
            int arity = extendedArguments.size();
	   		newPname.preThresholdedLiteral = literalToThreshold;
	   		if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%  Mode created (will be in-lined later):\n%     " + extendedLit); }
	   		if (printStream != null) printStream.println("mode: " + extendedLit + ".");
			stringHandler.recordMode(extendedLit);
			
			RelevanceStrength strength = literalToThreshold.predicateName.getRelevance(literalToThreshold.numberArgs());
			if (strength != null) { 
				stringHandler.setRelevance(extendedLit.predicateName, extendedLit.numberArgs(), strength);
				if (printStream != null) printStream.println("relevance: " + newPname + "/" + extendedLit.numberArgs() + " " + strength);
		   		if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%  With relevance: " + newPname + "/" + extendedLit.numberArgs() + " " + strength); }
			}
			
			// ARE THESE DONE AFTER THE RELEVANT SET IS CONSIDERED?
			
			innerLoopForILP.bodyModes.add( new PredicateNameAndArity(newPname, arity));
			
			if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%  Inform the pruning code that this is a 1D pruner with the thresholds as the last two arguments."); }
			newPname.setIsaInterval_1D(extendedArguments.size(), true); stringHandler.needPruner = true;
			if (printStream != null) printStream.println("isaInterval: " + newPname + "/" + extendedArguments.size() + ", 1D, boundariesAtEnd.");
			
	   		// Write BK for the new predicate.
	   		// Sample:		f_inBetween(X, Y, Value, Z, Lower, Upper)
	   		//					:- f(X, Y, Value, Z),
	   		//			   		   isa_f_inBetween(Lower, Upper),
	   		//			           Lower <= Value, Value < Upper.
	   		List<Term> variablizedArguments = new ArrayList<Term>(literalToThreshold.numberArgs());
	   		Variable   valueVar             = stringHandler.getGeneratedVariable(stringHandler.getVariablePrefix() + "Value" + (++valueCounter) + "_InThesh", true);
	   		for (int i = 1; i <= literalToThreshold.numberArgs(); i++) {
	   			if (i == literalToThreshold.positionToThreshold) { variablizedArguments.add(valueVar); }
	   			else                                             { variablizedArguments.add(stringHandler.getGeneratedVariable(stringHandler.getVariablePrefix() + "Var" + (++varCounter) + "_" + i + "_InThesh", true)); }
	   		}
	   		Variable lower = stringHandler.getGeneratedVariable(stringHandler.getVariablePrefix() + "Lower" + (++lowerCounter) + "_InThesh", true);
	   		Variable upper = stringHandler.getGeneratedVariable(stringHandler.getVariablePrefix() + "Upper" + (++upperCounter) + "_InThesh", true);
	   		List<Term> copyOfVariablizedArguments = new ArrayList<Term>(variablizedArguments.size());
	   		copyOfVariablizedArguments.addAll(variablizedArguments); // Need to make a COPY so when adding to variablizedArguments, this arguments to 'literalVariablized' are not also changed. 
	   		Literal literalVariablized = stringHandler.getLiteral(literalToThreshold.predicateName, copyOfVariablizedArguments);
	   		variablizedArguments.add(lower);
	   		variablizedArguments.add(upper);
	   		Literal headVariablized = stringHandler.getLiteral(newPname, variablizedArguments);
	   		PredicateName  isaPname = stringHandler.getPredicateName("isa_" + newPname.name);
	   		isaPname.addFilter();
	   		if (printStream != null) printStream.println("filter: " + isaPname + ". // This predicate should be deleted from a learned rule.");
	   		List<Term> args = new ArrayList<Term>(2);
	   		args.add(lower);
	   		args.add(upper);
	   		Literal isaInBetween    = stringHandler.getLiteral(isaPname, args);
	   		args = new ArrayList<Term>(2);  // Need fresh copy.
	   		args.add(lower);
	   		args.add(valueVar);
	   		Literal lte             = stringHandler.getLiteral(stringHandler.standardPredicateNames.lte, args);
	   		args = new ArrayList<Term>(2);  // Need fresh copy.
	   		args.add(valueVar);
	   		args.add(upper);
	   		Literal gt              = stringHandler.getLiteral(stringHandler.standardPredicateNames.lt,  args);
	   		Clause newBKclause      = stringHandler.getClause(headVariablized, true);
	   		newBKclause.negLiterals = new ArrayList<Literal>(4);
	   		newBKclause.negLiterals.add(literalVariablized);
	   		newBKclause.negLiterals.add(isaInBetween);
	   		newBKclause.negLiterals.add(lte);
	   		newBKclause.negLiterals.add(gt);
	   		if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%  New Background Rule:\n     " + newBKclause.toPrettyString("       ", Integer.MAX_VALUE) + "."); }
	   		if (printStream != null) printStream.println(newBKclause.toPrettyString("       ", Integer.MAX_VALUE) + ".");
	   		innerLoopForILP.addRule(newBKclause);
	   		inlineManager.addInventedClause(headVariablized, newBKclause);
	   		if (printStream != null) printStream.println("inline: " + headVariablized.predicateName + "/" + headVariablized.numberArgs() + ".");
	   		
	   		// Next create a NEGATED version of the interval TODO have a flag to override this.
	   		Literal  negatedHead      = headVariablized.copy(false);
	   		negatedHead.predicateName = stringHandler.getPredicateName("not_" + headVariablized.predicateName.name);
			PredicateName nbf      = stringHandler.standardPredicateNames.negationByFailure; // Negation by failure.
			List<Term>    nbf_args = new ArrayList<Term>(1);
			nbf_args.add(headVariablized.convertToFunction(stringHandler));
	   		Literal  negationByFailureOfHead = stringHandler.getLiteral(nbf, nbf_args);
	   		Clause   newNegatedBKclause      = stringHandler.getClause(negatedHead, true);
	   		newNegatedBKclause.negLiterals = new ArrayList<Literal>(1);
	   		newNegatedBKclause.negLiterals.add(negationByFailureOfHead);
	   		if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%  New Negation-by-Failure Background Rule:\n     " + newNegatedBKclause.toPrettyString("       ", Integer.MAX_VALUE) + "."); }
	   		if (printStream != null) printStream.println(newNegatedBKclause.toPrettyString("       ", Integer.MAX_VALUE) + ". // Negation-by-failure version.");
	   		innerLoopForILP.addRule(newNegatedBKclause);
	   		inlineManager.addInventedClause(negatedHead, newNegatedBKclause);
	   		if (printStream != null) printStream.println("inline: " + negatedHead.predicateName + "/" + negatedHead.numberArgs() + ".");	   
	   		if (printStream != null) printStream.println("mode: not_" + extendedLit + ".");		
	   		
	   		// Add to the facts, the boundaries associated with this new predicate.
	   		// Eg:
   			//     isa_f_inBetween(Double.NEGATIVE_INFINITY, split).
   			//     isa_f_inBetween(split, Double.POSITIVE_INFINITY).
	   		NumericConstant negInf      = stringHandler.getNumericConstant(Double.NEGATIVE_INFINITY);
	   		NumericConstant posInf      = stringHandler.getNumericConstant(Double.POSITIVE_INFINITY);
	   		int numbValues = class1values.size();
		   	if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%  New Background Facts:"); }
		   	int bkCounter = 0;
		   	List<Sentence> betweens = new ArrayList<Sentence>(4);  // Using Sentence here so addFacts() can be called at end.
	   		if (createTiles) {
	   			for (int i = 1; i <= numbValues; i++) { // Skip the first one, since no reason to have this be the upper bound, since no numbers less than it (recall that upper bounds are NOT in intervals).
	   				for (int j = -1; j < i - 1; j++) if (j >= 0 || i < numbValues) { // Don't generate the case [-inf, +inf].
	   			   		double          splitL0       = (j <  0          ? Double.NEGATIVE_INFINITY : class1values.get(j));
	   			   		double          splitL1       =                                               class1values.get(j + 1);
	   			   		double          splitU0       =                                               class1values.get(i - 1);
	   			   		double          splitU1       = (i == numbValues ? Double.POSITIVE_INFINITY : class1values.get(i));
	   			   		double          splitL        = (splitL0 + splitL1) / 2;
	   			   		double          splitU        = (splitU0 + splitU1) / 2;
	   			   		NumericConstant splitL_asTerm = (j <  0          ? negInf : stringHandler.getNumericConstant(splitL));
	   			   		NumericConstant splitU_asTerm = (i == numbValues ? posInf : stringHandler.getNumericConstant(splitU));
	   			   	
	   			   		args = new ArrayList<Term>(2); // Need a fresh copy.
	   			   		args.add(splitL_asTerm);
	   			   		args.add(splitU_asTerm);
	   			   		Literal isaBetween = stringHandler.getLiteral(isaPname, args);
	   			   		if (debugLevel > 0) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%      " + isaBetween + " // Interval #" + Utils.comma(++bkCounter)); }
	   			   		betweens.add(isaBetween);
	   					
	   				}
	   			}	   			
	   		} else for (int i = 1; i < numbValues; i++) {
		   		double          split       =  (class1values.get(i - 1) + class1values.get(i)) / 2;
		   		NumericConstant splitAsTerm = stringHandler.getNumericConstant(split);
		   		
		   		args = new ArrayList<Term>(2); // Need a fresh copy.
			   	args.add(negInf);
			   	args.add(splitAsTerm);
		   		Literal isaBetween1 = stringHandler.getLiteral(isaPname, args);
		   		args = new ArrayList<Term>(2); // Need a fresh copy.
			   	args.add(splitAsTerm);
			   	args.add(posInf);
		   		Literal isaBetween2 = stringHandler.getLiteral(isaPname, args);
		   		if (debugLevel > 0) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%      " + isaBetween1 + " // #" + Utils.comma(++bkCounter)); }
		   		if (debugLevel > 0) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%      " + isaBetween2 + " // #" + Utils.comma(++bkCounter)); }
		   		betweens.add(isaBetween1);	
		   		betweens.add(isaBetween2);
		   	}

			if (maxCuts > 0 && Utils.getSizeSafely(betweens) > maxCuts) {
				// TODO write better code for this, e.g. discard points closest to each other or use information gain.
				if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%   Reduce from " + Utils.comma(Utils.getSizeSafely(betweens)) + " intervals to the specified maximum of " + Utils.comma(maxCuts) + "."); }
				
				if (orgList1 != null && orgList2 != null) {
					if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%     Since we have two groups of values, will use information gain to score the candidate cuts."); }
					// If a large number of possible cuts, reduce to 100x the number we want before computing info gain.
					if (Utils.getSizeSafely(betweens) > 100 * maxCuts) { Utils.reduceToThisSizeByRandomlyDiscardingItemsInPlace(betweens, 100 * maxCuts); }
					List<ScoredSentence> scores = new ArrayList<ScoredSentence>(betweens.size());
					for (Sentence tweener : betweens) {
						Literal lit = (Literal) tweener;  // These will look like: 'isa_f_WILLinBetween(1.0, 3.0)'   Utils.println("lit = " + lit);
						double lowerLit = ((NumericConstant) lit.getArgument(0)).value.doubleValue();
						double upperLit = ((NumericConstant) lit.getArgument(1)).value.doubleValue();
						int    numberClass1Covered    = 0;
						int    numberClass2Covered    = 0;
						int    numberClass1NotCovered = 0;
						int    numberClass2NotCovered = 0;
						
						for (Double value : orgList1) {
							if (value >= lowerLit && value < upperLit) { numberClass1Covered++;    }
							else                                       { numberClass1NotCovered++; }
						}						
						for (Double value : orgList2) {
							if (value >= lowerLit && value < upperLit) { numberClass2Covered++;    }
							else                                       { numberClass2NotCovered++; }
						}
						
						int    covered     = numberClass1Covered    + numberClass2Covered;
						int    notCovered  = numberClass1NotCovered + numberClass2NotCovered;
						double total       = covered + notCovered;
						double fractTrue   = (total < 1 ? 0 : covered    / total);
						double fractFalse  = (total < 1 ? 0 : notCovered / total);
						double fract1true  = (covered    < 1 ? 0 : numberClass1Covered    / (double)covered);    // Of those true,  what fraction are of class 1?
						double fract1false = (notCovered < 1 ? 0 : numberClass1NotCovered / (double)notCovered); // Of those false, what fraction are of class 2?
						double infoLeft    = fractTrue * calcInfo(fract1true) + fractFalse * calcInfo(fract1false);
						
						scores.add(new ScoredSentence(tweener, infoLeft));
						
						// If a perfect split exists, keep it?  Maybe not, if tuning sets are being used ...
						
						// Fill half the quota using infoLeft and the other half randomly (since info needs to be conditioned on rest of clause).
						
						if (debugLevel > -10) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%      For '" + tweener +
												            "' covered " + Utils.comma(numberClass1Covered) + " of " + Utils.comma(numberClass1Covered + numberClass1NotCovered) + " positive examples and " +
			                                                " "          + Utils.comma(numberClass2Covered) + " of " + Utils.comma(numberClass2Covered + numberClass2NotCovered) + " negative examples.  Info left = " + Utils.truncate(infoLeft, 3));
						}
					}
					Collections.sort(scores, new ScoredSentenceComparator());
					if (debugLevel > 1) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%      Sorted: " + scores); }
					List<Sentence> newBetweens = new ArrayList<Sentence>(maxCuts);
					int counter = maxCuts / 2;  // Fill half with the BEST and the rest choose randomly (in case all the best cuts are near each other). TODO could use some clustering algorithm.
					for (ScoredSentence s : scores) {
						newBetweens.add(s.sentence);
						betweens.remove(s.sentence);
						if (--counter <= 0) { break; }
					}
					// Randomly discard some of the remaining 'betweens.'
					if (debugLevel > -1) { Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%      BEFORE reduction |betweens| = " + Utils.comma(betweens)); }
					Utils.reduceToThisSizeByRandomlyDiscardingItemsInPlace(betweens, maxCuts - Utils.getSizeSafely(newBetweens));
					newBetweens.addAll(betweens);
					if (debugLevel > -1) { 
						Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%      AFTER reduction  |betweens| = " + Utils.comma(betweens));
						Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "%      |newBetweens| = " + Utils.comma(newBetweens) + "  newBetweens: " + newBetweens);
					}
					betweens = newBetweens;
				} else {
					Utils.reduceToThisSizeByRandomlyDiscardingItemsInPlace(betweens, maxCuts);
				}
				
			}
			
			if (betweens != null) {
				innerLoopForILP.addFacts(betweens);
				int betweenCounter = 0;
				for (Sentence between : betweens) {
					betweenCounter++;
					Utils.println(MessageType.ILP_THESHOLDING_VERBOSE,                          "%  " + between + ". % #" + Utils.comma(betweenCounter));
					if (printStream != null) printStream.println(  between + ". % #" + Utils.comma(betweenCounter));
				}
			}
		} 
		if (debugLevel > -10) { 
			long end = System.currentTimeMillis();
			Utils.println(MessageType.ILP_THESHOLDING_VERBOSE, "% Time taken to compute thresholds for '" + literalToThreshold + "': " + Utils.convertMillisecondsToTimeSpan(end - start, 3) + ".");
		}
	}
	
	private double calcInfo(double fractionClass1) {
		double lg      = (   fractionClass1 > 1000 * Double.MIN_VALUE ? Math.log(    fractionClass1) / Math.log(2) : 0);
		double lgMinus = (1- fractionClass1 > 1000 * Double.MIN_VALUE ? Math.log(1 - fractionClass1) / Math.log(2) : 0);
		
		return -fractionClass1 * lg - (1 - fractionClass1) * lgMinus;
	}
}
class ScoredSentence {
	Sentence sentence;
	double   score;
	
	public ScoredSentence(Sentence sentence, double score) {
		super();
		this.sentence = sentence;
		this.score    = score;
	}	
	
	public String toString() {
		return sentence + "[" + score + "]";
	}
}
class ScoredSentenceComparator implements Comparator<ScoredSentence> {

	public int compare(ScoredSentence one, ScoredSentence two) {
		double sOne = one.score;
		double sTwo = two.score;
		
		if (sOne < sTwo) { return -1; }
		if (sOne > sTwo) { return  1; }
		return 0;
	}
}
