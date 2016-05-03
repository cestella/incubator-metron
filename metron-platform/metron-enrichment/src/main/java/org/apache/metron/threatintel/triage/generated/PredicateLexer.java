// Generated from org/apache/metron/threatintel/triage/generated/Predicate.g4 by ANTLR 4.5
package org.apache.metron.threatintel.triage.generated;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PredicateLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		AND=1, OR=2, TRUE=3, FALSE=4, EQ=5, NEQ=6, COMMA=7, LBRACKET=8, RBRACKET=9, 
		LPAREN=10, RPAREN=11, IN=12, NIN=13, EXISTS=14, IDENTIFIER=15, STRING_LITERAL=16, 
		SEMI=17, COMMENT=18, WS=19;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"AND", "OR", "TRUE", "FALSE", "EQ", "NEQ", "COMMA", "LBRACKET", "RBRACKET", 
		"LPAREN", "RPAREN", "IN", "NIN", "EXISTS", "IDENTIFIER", "SCHAR", "STRING_LITERAL", 
		"SEMI", "COMMENT", "WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, null, null, "'=='", "'!='", "','", "'['", "']'", "'('", 
		"')'", "'in'", "'not in'", "'exists'", null, null, "';'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "AND", "OR", "TRUE", "FALSE", "EQ", "NEQ", "COMMA", "LBRACKET", 
		"RBRACKET", "LPAREN", "RPAREN", "IN", "NIN", "EXISTS", "IDENTIFIER", "STRING_LITERAL", 
		"SEMI", "COMMENT", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public PredicateLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Predicate.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\25\u00a0\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\3\2\3\2\3\2\3\2\3\2\5\2\61\n\2\3\3"+
		"\3\3\3\3\3\3\5\3\67\n\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4A\n\4\3\5\3"+
		"\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5M\n\5\3\6\3\6\3\6\3\7\3\7\3\7\3"+
		"\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\r\3\16\3\16\3\16\3"+
		"\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\20\3\20\7\20r"+
		"\n\20\f\20\16\20u\13\20\3\21\3\21\3\22\3\22\7\22{\n\22\f\22\16\22~\13"+
		"\22\3\22\3\22\3\22\7\22\u0083\n\22\f\22\16\22\u0086\13\22\3\22\5\22\u0089"+
		"\n\22\3\23\3\23\3\24\3\24\3\24\3\24\6\24\u0091\n\24\r\24\16\24\u0092\3"+
		"\24\5\24\u0096\n\24\3\24\3\24\3\25\6\25\u009b\n\25\r\25\16\25\u009c\3"+
		"\25\3\25\3\u0092\2\26\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27"+
		"\r\31\16\33\17\35\20\37\21!\2#\22%\23\'\24)\25\3\2\7\5\2C\\aac|\b\2\60"+
		"\60\62;C\\^^aac|\7\2\f\f\17\17$$))^^\3\3\f\f\5\2\13\f\16\17\"\"\u00a8"+
		"\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2"+
		"\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2"+
		"\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2#\3\2\2\2\2%\3\2"+
		"\2\2\2\'\3\2\2\2\2)\3\2\2\2\3\60\3\2\2\2\5\66\3\2\2\2\7@\3\2\2\2\tL\3"+
		"\2\2\2\13N\3\2\2\2\rQ\3\2\2\2\17T\3\2\2\2\21V\3\2\2\2\23X\3\2\2\2\25Z"+
		"\3\2\2\2\27\\\3\2\2\2\31^\3\2\2\2\33a\3\2\2\2\35h\3\2\2\2\37o\3\2\2\2"+
		"!v\3\2\2\2#\u0088\3\2\2\2%\u008a\3\2\2\2\'\u008c\3\2\2\2)\u009a\3\2\2"+
		"\2+,\7c\2\2,-\7p\2\2-\61\7f\2\2./\7(\2\2/\61\7(\2\2\60+\3\2\2\2\60.\3"+
		"\2\2\2\61\4\3\2\2\2\62\63\7q\2\2\63\67\7t\2\2\64\65\7~\2\2\65\67\7~\2"+
		"\2\66\62\3\2\2\2\66\64\3\2\2\2\67\6\3\2\2\289\7v\2\29:\7t\2\2:;\7w\2\2"+
		";A\7g\2\2<=\7V\2\2=>\7T\2\2>?\7W\2\2?A\7G\2\2@8\3\2\2\2@<\3\2\2\2A\b\3"+
		"\2\2\2BC\7h\2\2CD\7c\2\2DE\7n\2\2EF\7u\2\2FM\7g\2\2GH\7H\2\2HI\7C\2\2"+
		"IJ\7N\2\2JK\7U\2\2KM\7G\2\2LB\3\2\2\2LG\3\2\2\2M\n\3\2\2\2NO\7?\2\2OP"+
		"\7?\2\2P\f\3\2\2\2QR\7#\2\2RS\7?\2\2S\16\3\2\2\2TU\7.\2\2U\20\3\2\2\2"+
		"VW\7]\2\2W\22\3\2\2\2XY\7_\2\2Y\24\3\2\2\2Z[\7*\2\2[\26\3\2\2\2\\]\7+"+
		"\2\2]\30\3\2\2\2^_\7k\2\2_`\7p\2\2`\32\3\2\2\2ab\7p\2\2bc\7q\2\2cd\7v"+
		"\2\2de\7\"\2\2ef\7k\2\2fg\7p\2\2g\34\3\2\2\2hi\7g\2\2ij\7z\2\2jk\7k\2"+
		"\2kl\7u\2\2lm\7v\2\2mn\7u\2\2n\36\3\2\2\2os\t\2\2\2pr\t\3\2\2qp\3\2\2"+
		"\2ru\3\2\2\2sq\3\2\2\2st\3\2\2\2t \3\2\2\2us\3\2\2\2vw\n\4\2\2w\"\3\2"+
		"\2\2x|\7$\2\2y{\5!\21\2zy\3\2\2\2{~\3\2\2\2|z\3\2\2\2|}\3\2\2\2}\177\3"+
		"\2\2\2~|\3\2\2\2\177\u0089\7$\2\2\u0080\u0084\7)\2\2\u0081\u0083\5!\21"+
		"\2\u0082\u0081\3\2\2\2\u0083\u0086\3\2\2\2\u0084\u0082\3\2\2\2\u0084\u0085"+
		"\3\2\2\2\u0085\u0087\3\2\2\2\u0086\u0084\3\2\2\2\u0087\u0089\7)\2\2\u0088"+
		"x\3\2\2\2\u0088\u0080\3\2\2\2\u0089$\3\2\2\2\u008a\u008b\7=\2\2\u008b"+
		"&\3\2\2\2\u008c\u008d\7\61\2\2\u008d\u008e\7\61\2\2\u008e\u0090\3\2\2"+
		"\2\u008f\u0091\13\2\2\2\u0090\u008f\3\2\2\2\u0091\u0092\3\2\2\2\u0092"+
		"\u0093\3\2\2\2\u0092\u0090\3\2\2\2\u0093\u0095\3\2\2\2\u0094\u0096\t\5"+
		"\2\2\u0095\u0094\3\2\2\2\u0096\u0097\3\2\2\2\u0097\u0098\b\24\2\2\u0098"+
		"(\3\2\2\2\u0099\u009b\t\6\2\2\u009a\u0099\3\2\2\2\u009b\u009c\3\2\2\2"+
		"\u009c\u009a\3\2\2\2\u009c\u009d\3\2\2\2\u009d\u009e\3\2\2\2\u009e\u009f"+
		"\b\25\2\2\u009f*\3\2\2\2\16\2\60\66@Ls|\u0084\u0088\u0092\u0095\u009c"+
		"\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}