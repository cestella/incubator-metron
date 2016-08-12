// Generated from org/apache/metron/common/stellar/generated/Stellar.g4 by ANTLR 4.5
package org.apache.metron.common.stellar.generated;

//CHECKSTYLE:OFF
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
public class StellarLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, COMMA=2, AND=3, OR=4, NOT=5, TRUE=6, FALSE=7, EQ=8, NEQ=9, LT=10, 
		LTE=11, GT=12, GTE=13, QUESTION=14, COLON=15, IF=16, NULL=17, MINUS=18, 
		PLUS=19, DIV=20, MUL=21, LBRACE=22, RBRACE=23, LBRACKET=24, RBRACKET=25, 
		LPAREN=26, RPAREN=27, IN=28, NIN=29, EXISTS=30, INT_LITERAL=31, DOUBLE_LITERAL=32, 
		IDENTIFIER=33, STRING_LITERAL=34, COMMENT=35, WS=36;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "COMMA", "AND", "OR", "NOT", "TRUE", "FALSE", "EQ", "NEQ", "LT", 
		"LTE", "GT", "GTE", "QUESTION", "COLON", "IF", "NULL", "MINUS", "PLUS", 
		"DIV", "MUL", "LBRACE", "RBRACE", "LBRACKET", "RBRACKET", "LPAREN", "RPAREN", 
		"IN", "NIN", "EXISTS", "INT_LITERAL", "DOUBLE_LITERAL", "IDENTIFIER", 
		"SCHAR", "STRING_LITERAL", "COMMENT", "WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "':'", "','", null, null, null, null, null, "'=='", "'!='", "'<'", 
		"'<='", "'>'", "'>='", null, null, null, null, "'-'", "'+'", "'/'", "'*'", 
		"'{'", "'}'", "'['", "']'", "'('", "')'", "'in'", "'not in'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, "COMMA", "AND", "OR", "NOT", "TRUE", "FALSE", "EQ", "NEQ", 
		"LT", "LTE", "GT", "GTE", "QUESTION", "COLON", "IF", "NULL", "MINUS", 
		"PLUS", "DIV", "MUL", "LBRACE", "RBRACE", "LBRACKET", "RBRACKET", "LPAREN", 
		"RPAREN", "IN", "NIN", "EXISTS", "INT_LITERAL", "DOUBLE_LITERAL", "IDENTIFIER", 
		"STRING_LITERAL", "COMMENT", "WS"
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


	public StellarLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Stellar.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2&\u0128\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\3\2\3\2\3\3\3\3\3\4\3\4\3\4\3\4\3"+
		"\4\3\4\3\4\3\4\5\4Z\n\4\3\5\3\5\3\5\3\5\3\5\3\5\5\5b\n\5\3\6\3\6\3\6\3"+
		"\6\3\6\3\6\5\6j\n\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\5\7t\n\7\3\b\3\b\3"+
		"\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u0080\n\b\3\t\3\t\3\t\3\n\3\n\3\n\3"+
		"\13\3\13\3\f\3\f\3\f\3\r\3\r\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3"+
		"\17\3\17\3\17\3\17\5\17\u009b\n\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20"+
		"\3\20\3\20\5\20\u00a6\n\20\3\21\3\21\3\21\3\21\5\21\u00ac\n\21\3\22\3"+
		"\22\3\22\3\22\3\22\3\22\3\22\3\22\5\22\u00b6\n\22\3\23\3\23\3\24\3\24"+
		"\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33"+
		"\3\34\3\34\3\35\3\35\3\35\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\37\3\37"+
		"\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\5\37\u00e2\n\37\3 "+
		"\5 \u00e5\n \3 \6 \u00e8\n \r \16 \u00e9\3!\5!\u00ed\n!\3!\6!\u00f0\n"+
		"!\r!\16!\u00f1\3!\3!\6!\u00f6\n!\r!\16!\u00f7\3\"\3\"\7\"\u00fc\n\"\f"+
		"\"\16\"\u00ff\13\"\3#\3#\3$\3$\7$\u0105\n$\f$\16$\u0108\13$\3$\3$\3$\7"+
		"$\u010d\n$\f$\16$\u0110\13$\3$\5$\u0113\n$\3%\3%\3%\3%\6%\u0119\n%\r%"+
		"\16%\u011a\3%\5%\u011e\n%\3%\3%\3&\6&\u0123\n&\r&\16&\u0124\3&\3&\3\u011a"+
		"\2\'\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35"+
		"\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36"+
		";\37= ?!A\"C#E\2G$I%K&\3\2\7\5\2C\\aac|\b\2\60\60\62;C\\^^aac|\7\2\f\f"+
		"\17\17$$))^^\3\3\f\f\5\2\13\f\16\17\"\"\u013f\2\3\3\2\2\2\2\5\3\2\2\2"+
		"\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3"+
		"\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2"+
		"\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2"+
		"\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2"+
		"\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2"+
		"\2\2\2A\3\2\2\2\2C\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\3M\3\2\2\2"+
		"\5O\3\2\2\2\7Y\3\2\2\2\ta\3\2\2\2\13i\3\2\2\2\rs\3\2\2\2\17\177\3\2\2"+
		"\2\21\u0081\3\2\2\2\23\u0084\3\2\2\2\25\u0087\3\2\2\2\27\u0089\3\2\2\2"+
		"\31\u008c\3\2\2\2\33\u008e\3\2\2\2\35\u009a\3\2\2\2\37\u00a5\3\2\2\2!"+
		"\u00ab\3\2\2\2#\u00b5\3\2\2\2%\u00b7\3\2\2\2\'\u00b9\3\2\2\2)\u00bb\3"+
		"\2\2\2+\u00bd\3\2\2\2-\u00bf\3\2\2\2/\u00c1\3\2\2\2\61\u00c3\3\2\2\2\63"+
		"\u00c5\3\2\2\2\65\u00c7\3\2\2\2\67\u00c9\3\2\2\29\u00cb\3\2\2\2;\u00ce"+
		"\3\2\2\2=\u00e1\3\2\2\2?\u00e4\3\2\2\2A\u00ec\3\2\2\2C\u00f9\3\2\2\2E"+
		"\u0100\3\2\2\2G\u0112\3\2\2\2I\u0114\3\2\2\2K\u0122\3\2\2\2MN\7<\2\2N"+
		"\4\3\2\2\2OP\7.\2\2P\6\3\2\2\2QR\7c\2\2RS\7p\2\2SZ\7f\2\2TU\7(\2\2UZ\7"+
		"(\2\2VW\7C\2\2WX\7P\2\2XZ\7F\2\2YQ\3\2\2\2YT\3\2\2\2YV\3\2\2\2Z\b\3\2"+
		"\2\2[\\\7q\2\2\\b\7t\2\2]^\7~\2\2^b\7~\2\2_`\7Q\2\2`b\7T\2\2a[\3\2\2\2"+
		"a]\3\2\2\2a_\3\2\2\2b\n\3\2\2\2cd\7p\2\2de\7q\2\2ej\7v\2\2fg\7P\2\2gh"+
		"\7Q\2\2hj\7V\2\2ic\3\2\2\2if\3\2\2\2j\f\3\2\2\2kl\7v\2\2lm\7t\2\2mn\7"+
		"w\2\2nt\7g\2\2op\7V\2\2pq\7T\2\2qr\7W\2\2rt\7G\2\2sk\3\2\2\2so\3\2\2\2"+
		"t\16\3\2\2\2uv\7h\2\2vw\7c\2\2wx\7n\2\2xy\7u\2\2y\u0080\7g\2\2z{\7H\2"+
		"\2{|\7C\2\2|}\7N\2\2}~\7U\2\2~\u0080\7G\2\2\177u\3\2\2\2\177z\3\2\2\2"+
		"\u0080\20\3\2\2\2\u0081\u0082\7?\2\2\u0082\u0083\7?\2\2\u0083\22\3\2\2"+
		"\2\u0084\u0085\7#\2\2\u0085\u0086\7?\2\2\u0086\24\3\2\2\2\u0087\u0088"+
		"\7>\2\2\u0088\26\3\2\2\2\u0089\u008a\7>\2\2\u008a\u008b\7?\2\2\u008b\30"+
		"\3\2\2\2\u008c\u008d\7@\2\2\u008d\32\3\2\2\2\u008e\u008f\7@\2\2\u008f"+
		"\u0090\7?\2\2\u0090\34\3\2\2\2\u0091\u009b\7A\2\2\u0092\u0093\7V\2\2\u0093"+
		"\u0094\7J\2\2\u0094\u0095\7G\2\2\u0095\u009b\7P\2\2\u0096\u0097\7v\2\2"+
		"\u0097\u0098\7j\2\2\u0098\u0099\7g\2\2\u0099\u009b\7p\2\2\u009a\u0091"+
		"\3\2\2\2\u009a\u0092\3\2\2\2\u009a\u0096\3\2\2\2\u009b\36\3\2\2\2\u009c"+
		"\u00a6\7<\2\2\u009d\u009e\7G\2\2\u009e\u009f\7N\2\2\u009f\u00a0\7U\2\2"+
		"\u00a0\u00a6\7G\2\2\u00a1\u00a2\7g\2\2\u00a2\u00a3\7n\2\2\u00a3\u00a4"+
		"\7u\2\2\u00a4\u00a6\7g\2\2\u00a5\u009c\3\2\2\2\u00a5\u009d\3\2\2\2\u00a5"+
		"\u00a1\3\2\2\2\u00a6 \3\2\2\2\u00a7\u00a8\7K\2\2\u00a8\u00ac\7H\2\2\u00a9"+
		"\u00aa\7k\2\2\u00aa\u00ac\7h\2\2\u00ab\u00a7\3\2\2\2\u00ab\u00a9\3\2\2"+
		"\2\u00ac\"\3\2\2\2\u00ad\u00ae\7p\2\2\u00ae\u00af\7w\2\2\u00af\u00b0\7"+
		"n\2\2\u00b0\u00b6\7n\2\2\u00b1\u00b2\7P\2\2\u00b2\u00b3\7W\2\2\u00b3\u00b4"+
		"\7N\2\2\u00b4\u00b6\7N\2\2\u00b5\u00ad\3\2\2\2\u00b5\u00b1\3\2\2\2\u00b6"+
		"$\3\2\2\2\u00b7\u00b8\7/\2\2\u00b8&\3\2\2\2\u00b9\u00ba\7-\2\2\u00ba("+
		"\3\2\2\2\u00bb\u00bc\7\61\2\2\u00bc*\3\2\2\2\u00bd\u00be\7,\2\2\u00be"+
		",\3\2\2\2\u00bf\u00c0\7}\2\2\u00c0.\3\2\2\2\u00c1\u00c2\7\177\2\2\u00c2"+
		"\60\3\2\2\2\u00c3\u00c4\7]\2\2\u00c4\62\3\2\2\2\u00c5\u00c6\7_\2\2\u00c6"+
		"\64\3\2\2\2\u00c7\u00c8\7*\2\2\u00c8\66\3\2\2\2\u00c9\u00ca\7+\2\2\u00ca"+
		"8\3\2\2\2\u00cb\u00cc\7k\2\2\u00cc\u00cd\7p\2\2\u00cd:\3\2\2\2\u00ce\u00cf"+
		"\7p\2\2\u00cf\u00d0\7q\2\2\u00d0\u00d1\7v\2\2\u00d1\u00d2\7\"\2\2\u00d2"+
		"\u00d3\7k\2\2\u00d3\u00d4\7p\2\2\u00d4<\3\2\2\2\u00d5\u00d6\7g\2\2\u00d6"+
		"\u00d7\7z\2\2\u00d7\u00d8\7k\2\2\u00d8\u00d9\7u\2\2\u00d9\u00da\7v\2\2"+
		"\u00da\u00e2\7u\2\2\u00db\u00dc\7G\2\2\u00dc\u00dd\7Z\2\2\u00dd\u00de"+
		"\7K\2\2\u00de\u00df\7U\2\2\u00df\u00e0\7V\2\2\u00e0\u00e2\7U\2\2\u00e1"+
		"\u00d5\3\2\2\2\u00e1\u00db\3\2\2\2\u00e2>\3\2\2\2\u00e3\u00e5\5%\23\2"+
		"\u00e4\u00e3\3\2\2\2\u00e4\u00e5\3\2\2\2\u00e5\u00e7\3\2\2\2\u00e6\u00e8"+
		"\4\62;\2\u00e7\u00e6\3\2\2\2\u00e8\u00e9\3\2\2\2\u00e9\u00e7\3\2\2\2\u00e9"+
		"\u00ea\3\2\2\2\u00ea@\3\2\2\2\u00eb\u00ed\5%\23\2\u00ec\u00eb\3\2\2\2"+
		"\u00ec\u00ed\3\2\2\2\u00ed\u00ef\3\2\2\2\u00ee\u00f0\4\62;\2\u00ef\u00ee"+
		"\3\2\2\2\u00f0\u00f1\3\2\2\2\u00f1\u00ef\3\2\2\2\u00f1\u00f2\3\2\2\2\u00f2"+
		"\u00f3\3\2\2\2\u00f3\u00f5\7\60\2\2\u00f4\u00f6\4\62;\2\u00f5\u00f4\3"+
		"\2\2\2\u00f6\u00f7\3\2\2\2\u00f7\u00f5\3\2\2\2\u00f7\u00f8\3\2\2\2\u00f8"+
		"B\3\2\2\2\u00f9\u00fd\t\2\2\2\u00fa\u00fc\t\3\2\2\u00fb\u00fa\3\2\2\2"+
		"\u00fc\u00ff\3\2\2\2\u00fd\u00fb\3\2\2\2\u00fd\u00fe\3\2\2\2\u00feD\3"+
		"\2\2\2\u00ff\u00fd\3\2\2\2\u0100\u0101\n\4\2\2\u0101F\3\2\2\2\u0102\u0106"+
		"\7$\2\2\u0103\u0105\5E#\2\u0104\u0103\3\2\2\2\u0105\u0108\3\2\2\2\u0106"+
		"\u0104\3\2\2\2\u0106\u0107\3\2\2\2\u0107\u0109\3\2\2\2\u0108\u0106\3\2"+
		"\2\2\u0109\u0113\7$\2\2\u010a\u010e\7)\2\2\u010b\u010d\5E#\2\u010c\u010b"+
		"\3\2\2\2\u010d\u0110\3\2\2\2\u010e\u010c\3\2\2\2\u010e\u010f\3\2\2\2\u010f"+
		"\u0111\3\2\2\2\u0110\u010e\3\2\2\2\u0111\u0113\7)\2\2\u0112\u0102\3\2"+
		"\2\2\u0112\u010a\3\2\2\2\u0113H\3\2\2\2\u0114\u0115\7\61\2\2\u0115\u0116"+
		"\7\61\2\2\u0116\u0118\3\2\2\2\u0117\u0119\13\2\2\2\u0118\u0117\3\2\2\2"+
		"\u0119\u011a\3\2\2\2\u011a\u011b\3\2\2\2\u011a\u0118\3\2\2\2\u011b\u011d"+
		"\3\2\2\2\u011c\u011e\t\5\2\2\u011d\u011c\3\2\2\2\u011e\u011f\3\2\2\2\u011f"+
		"\u0120\b%\2\2\u0120J\3\2\2\2\u0121\u0123\t\6\2\2\u0122\u0121\3\2\2\2\u0123"+
		"\u0124\3\2\2\2\u0124\u0122\3\2\2\2\u0124\u0125\3\2\2\2\u0125\u0126\3\2"+
		"\2\2\u0126\u0127\b&\2\2\u0127L\3\2\2\2\31\2Yais\177\u009a\u00a5\u00ab"+
		"\u00b5\u00e1\u00e4\u00e9\u00ec\u00f1\u00f7\u00fd\u0106\u010e\u0112\u011a"+
		"\u011d\u0124\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}