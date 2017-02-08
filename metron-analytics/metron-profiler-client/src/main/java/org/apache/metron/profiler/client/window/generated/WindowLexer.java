// Generated from org/apache/metron/profiler/client/window/generated/Window.g4 by ANTLR 4.5
package org.apache.metron.profiler.client.window.generated;

//CHECKSTYLE:OFF
/*
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
public class WindowLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		COMMA=1, BIN=2, INCLUDE=3, EXCLUDE=4, NOW=5, FROM=6, EVERY=7, TO=8, AGO=9, 
		NUMBER=10, WS=11, DAY_SPECIFIER=12, TIME_UNIT=13;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"COMMA", "BIN", "INCLUDE", "EXCLUDE", "NOW", "FROM", "EVERY", "TO", "AGO", 
		"NUMBER", "WS", "DAY_SPECIFIER", "TIME_UNIT", "SECOND_UNIT", "HOUR_UNIT", 
		"DAY_UNIT", "MONTH_UNIT", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", 
		"FRIDAY", "SATURDAY", "SUNDAY", "CURRENT_DAY_OF_WEEK", "WEEKEND", "WEEKDAY", 
		"DIGIT", "FIRST_DIGIT"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "','"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "COMMA", "BIN", "INCLUDE", "EXCLUDE", "NOW", "FROM", "EVERY", "TO", 
		"AGO", "NUMBER", "WS", "DAY_SPECIFIER", "TIME_UNIT"
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


	public WindowLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Window.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\17\u0215\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\3\2\3\2\3\3\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3N\n\3\3\4\3\4\3"+
		"\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4"+
		"\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4n\n\4\3\5\3\5\3\5\3\5\3\5"+
		"\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3"+
		"\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5\u008e\n\5\3\6\3\6\3\6\3\6\3\6\3\6\5"+
		"\6\u0096\n\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\5\7\u00a0\n\7\3\b\3\b\3\b"+
		"\3\b\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u00ac\n\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\t\u00bc\n\t\3\n\3\n\3\n\3\n\3\n\3\n\5\n"+
		"\u00c4\n\n\3\13\3\13\7\13\u00c8\n\13\f\13\16\13\u00cb\13\13\3\f\6\f\u00ce"+
		"\n\f\r\f\16\f\u00cf\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\5"+
		"\r\u00de\n\r\3\16\3\16\3\16\3\16\5\16\u00e4\n\16\3\17\3\17\3\17\3\17\3"+
		"\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3"+
		"\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\5\17\u0100\n\17\3\20\3\20\3\20"+
		"\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20"+
		"\3\20\5\20\u0114\n\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\5\21\u0124\n\21\3\22\3\22\3\22\3\22\3\22\3\22\3\22"+
		"\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22"+
		"\3\22\5\22\u013c\n\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23"+
		"\3\23\3\23\5\23\u014a\n\23\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24"+
		"\3\24\3\24\3\24\3\24\3\24\5\24\u015a\n\24\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\5\25\u016e"+
		"\n\25\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26"+
		"\3\26\3\26\3\26\5\26\u0180\n\26\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27"+
		"\3\27\3\27\3\27\3\27\5\27\u018e\n\27\3\30\3\30\3\30\3\30\3\30\3\30\3\30"+
		"\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\5\30\u01a0\n\30\3\31\3\31"+
		"\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\5\31\u01ae\n\31\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\5\32\u01d0\n\32\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33"+
		"\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33"+
		"\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\5\33\u01f0\n\33\3\34\3\34\3\34"+
		"\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34"+
		"\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\5\34"+
		"\u0210\n\34\3\35\3\35\3\36\3\36\2\2\37\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21"+
		"\n\23\13\25\f\27\r\31\16\33\17\35\2\37\2!\2#\2%\2\'\2)\2+\2-\2/\2\61\2"+
		"\63\2\65\2\67\29\2;\2\3\2\3\5\2\13\f\16\17\"\"\u023c\2\3\3\2\2\2\2\5\3"+
		"\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2"+
		"\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3"+
		"\2\2\2\3=\3\2\2\2\5M\3\2\2\2\7m\3\2\2\2\t\u008d\3\2\2\2\13\u0095\3\2\2"+
		"\2\r\u009f\3\2\2\2\17\u00ab\3\2\2\2\21\u00bb\3\2\2\2\23\u00c3\3\2\2\2"+
		"\25\u00c5\3\2\2\2\27\u00cd\3\2\2\2\31\u00dd\3\2\2\2\33\u00e3\3\2\2\2\35"+
		"\u00ff\3\2\2\2\37\u0113\3\2\2\2!\u0123\3\2\2\2#\u013b\3\2\2\2%\u0149\3"+
		"\2\2\2\'\u0159\3\2\2\2)\u016d\3\2\2\2+\u017f\3\2\2\2-\u018d\3\2\2\2/\u019f"+
		"\3\2\2\2\61\u01ad\3\2\2\2\63\u01cf\3\2\2\2\65\u01ef\3\2\2\2\67\u020f\3"+
		"\2\2\29\u0211\3\2\2\2;\u0213\3\2\2\2=>\7.\2\2>\4\3\2\2\2?@\7d\2\2@A\7"+
		"k\2\2AN\7p\2\2BC\7D\2\2CD\7K\2\2DN\7P\2\2EF\7d\2\2FG\7k\2\2GH\7p\2\2H"+
		"N\7u\2\2IJ\7D\2\2JK\7K\2\2KL\7P\2\2LN\7U\2\2M?\3\2\2\2MB\3\2\2\2ME\3\2"+
		"\2\2MI\3\2\2\2N\6\3\2\2\2OP\7k\2\2PQ\7p\2\2QR\7e\2\2RS\7n\2\2ST\7w\2\2"+
		"TU\7f\2\2Un\7g\2\2VW\7K\2\2WX\7P\2\2XY\7E\2\2YZ\7N\2\2Z[\7W\2\2[\\\7F"+
		"\2\2\\n\7G\2\2]^\7k\2\2^_\7p\2\2_`\7e\2\2`a\7n\2\2ab\7w\2\2bc\7f\2\2c"+
		"d\7g\2\2dn\7u\2\2ef\7K\2\2fg\7P\2\2gh\7E\2\2hi\7N\2\2ij\7W\2\2jk\7F\2"+
		"\2kl\7G\2\2ln\7U\2\2mO\3\2\2\2mV\3\2\2\2m]\3\2\2\2me\3\2\2\2n\b\3\2\2"+
		"\2op\7g\2\2pq\7z\2\2qr\7e\2\2rs\7n\2\2st\7w\2\2tu\7f\2\2u\u008e\7g\2\2"+
		"vw\7G\2\2wx\7Z\2\2xy\7E\2\2yz\7N\2\2z{\7W\2\2{|\7F\2\2|\u008e\7G\2\2}"+
		"~\7g\2\2~\177\7z\2\2\177\u0080\7e\2\2\u0080\u0081\7n\2\2\u0081\u0082\7"+
		"w\2\2\u0082\u0083\7f\2\2\u0083\u0084\7g\2\2\u0084\u008e\7u\2\2\u0085\u0086"+
		"\7G\2\2\u0086\u0087\7Z\2\2\u0087\u0088\7E\2\2\u0088\u0089\7N\2\2\u0089"+
		"\u008a\7W\2\2\u008a\u008b\7F\2\2\u008b\u008c\7G\2\2\u008c\u008e\7U\2\2"+
		"\u008do\3\2\2\2\u008dv\3\2\2\2\u008d}\3\2\2\2\u008d\u0085\3\2\2\2\u008e"+
		"\n\3\2\2\2\u008f\u0090\7P\2\2\u0090\u0091\7Q\2\2\u0091\u0096\7Y\2\2\u0092"+
		"\u0093\7p\2\2\u0093\u0094\7q\2\2\u0094\u0096\7y\2\2\u0095\u008f\3\2\2"+
		"\2\u0095\u0092\3\2\2\2\u0096\f\3\2\2\2\u0097\u0098\7H\2\2\u0098\u0099"+
		"\7T\2\2\u0099\u009a\7Q\2\2\u009a\u00a0\7O\2\2\u009b\u009c\7h\2\2\u009c"+
		"\u009d\7t\2\2\u009d\u009e\7q\2\2\u009e\u00a0\7o\2\2\u009f\u0097\3\2\2"+
		"\2\u009f\u009b\3\2\2\2\u00a0\16\3\2\2\2\u00a1\u00a2\7G\2\2\u00a2\u00a3"+
		"\7X\2\2\u00a3\u00a4\7G\2\2\u00a4\u00a5\7T\2\2\u00a5\u00ac\7[\2\2\u00a6"+
		"\u00a7\7g\2\2\u00a7\u00a8\7x\2\2\u00a8\u00a9\7g\2\2\u00a9\u00aa\7t\2\2"+
		"\u00aa\u00ac\7{\2\2\u00ab\u00a1\3\2\2\2\u00ab\u00a6\3\2\2\2\u00ac\20\3"+
		"\2\2\2\u00ad\u00ae\7V\2\2\u00ae\u00bc\7Q\2\2\u00af\u00b0\7v\2\2\u00b0"+
		"\u00bc\7q\2\2\u00b1\u00b2\7w\2\2\u00b2\u00b3\7p\2\2\u00b3\u00b4\7v\2\2"+
		"\u00b4\u00b5\7k\2\2\u00b5\u00bc\7n\2\2\u00b6\u00b7\7W\2\2\u00b7\u00b8"+
		"\7P\2\2\u00b8\u00b9\7V\2\2\u00b9\u00ba\7K\2\2\u00ba\u00bc\7N\2\2\u00bb"+
		"\u00ad\3\2\2\2\u00bb\u00af\3\2\2\2\u00bb\u00b1\3\2\2\2\u00bb\u00b6\3\2"+
		"\2\2\u00bc\22\3\2\2\2\u00bd\u00be\7C\2\2\u00be\u00bf\7I\2\2\u00bf\u00c4"+
		"\7Q\2\2\u00c0\u00c1\7c\2\2\u00c1\u00c2\7i\2\2\u00c2\u00c4\7q\2\2\u00c3"+
		"\u00bd\3\2\2\2\u00c3\u00c0\3\2\2\2\u00c4\24\3\2\2\2\u00c5\u00c9\5;\36"+
		"\2\u00c6\u00c8\59\35\2\u00c7\u00c6\3\2\2\2\u00c8\u00cb\3\2\2\2\u00c9\u00c7"+
		"\3\2\2\2\u00c9\u00ca\3\2\2\2\u00ca\26\3\2\2\2\u00cb\u00c9\3\2\2\2\u00cc"+
		"\u00ce\t\2\2\2\u00cd\u00cc\3\2\2\2\u00ce\u00cf\3\2\2\2\u00cf\u00cd\3\2"+
		"\2\2\u00cf\u00d0\3\2\2\2\u00d0\u00d1\3\2\2\2\u00d1\u00d2\b\f\2\2\u00d2"+
		"\30\3\2\2\2\u00d3\u00de\5%\23\2\u00d4\u00de\5\'\24\2\u00d5\u00de\5)\25"+
		"\2\u00d6\u00de\5+\26\2\u00d7\u00de\5-\27\2\u00d8\u00de\5/\30\2\u00d9\u00de"+
		"\5\61\31\2\u00da\u00de\5\63\32\2\u00db\u00de\5\65\33\2\u00dc\u00de\5\67"+
		"\34\2\u00dd\u00d3\3\2\2\2\u00dd\u00d4\3\2\2\2\u00dd\u00d5\3\2\2\2\u00dd"+
		"\u00d6\3\2\2\2\u00dd\u00d7\3\2\2\2\u00dd\u00d8\3\2\2\2\u00dd\u00d9\3\2"+
		"\2\2\u00dd\u00da\3\2\2\2\u00dd\u00db\3\2\2\2\u00dd\u00dc\3\2\2\2\u00de"+
		"\32\3\2\2\2\u00df\u00e4\5\35\17\2\u00e0\u00e4\5\37\20\2\u00e1\u00e4\5"+
		"!\21\2\u00e2\u00e4\5#\22\2\u00e3\u00df\3\2\2\2\u00e3\u00e0\3\2\2\2\u00e3"+
		"\u00e1\3\2\2\2\u00e3\u00e2\3\2\2\2\u00e4\34\3\2\2\2\u00e5\u00e6\7U\2\2"+
		"\u00e6\u00e7\7G\2\2\u00e7\u00e8\7E\2\2\u00e8\u00e9\7Q\2\2\u00e9\u00ea"+
		"\7P\2\2\u00ea\u0100\7F\2\2\u00eb\u00ec\7u\2\2\u00ec\u00ed\7g\2\2\u00ed"+
		"\u00ee\7e\2\2\u00ee\u00ef\7q\2\2\u00ef\u00f0\7p\2\2\u00f0\u0100\7f\2\2"+
		"\u00f1\u00f2\7u\2\2\u00f2\u00f3\7g\2\2\u00f3\u00f4\7e\2\2\u00f4\u00f5"+
		"\7q\2\2\u00f5\u00f6\7p\2\2\u00f6\u00f7\7f\2\2\u00f7\u0100\7u\2\2\u00f8"+
		"\u00f9\7U\2\2\u00f9\u00fa\7G\2\2\u00fa\u00fb\7E\2\2\u00fb\u00fc\7Q\2\2"+
		"\u00fc\u00fd\7P\2\2\u00fd\u00fe\7F\2\2\u00fe\u0100\7U\2\2\u00ff\u00e5"+
		"\3\2\2\2\u00ff\u00eb\3\2\2\2\u00ff\u00f1\3\2\2\2\u00ff\u00f8\3\2\2\2\u0100"+
		"\36\3\2\2\2\u0101\u0102\7J\2\2\u0102\u0103\7Q\2\2\u0103\u0104\7W\2\2\u0104"+
		"\u0114\7T\2\2\u0105\u0106\7j\2\2\u0106\u0107\7q\2\2\u0107\u0108\7w\2\2"+
		"\u0108\u0114\7t\2\2\u0109\u010a\7j\2\2\u010a\u010b\7q\2\2\u010b\u010c"+
		"\7w\2\2\u010c\u010d\7t\2\2\u010d\u0114\7u\2\2\u010e\u010f\7J\2\2\u010f"+
		"\u0110\7Q\2\2\u0110\u0111\7W\2\2\u0111\u0112\7T\2\2\u0112\u0114\7U\2\2"+
		"\u0113\u0101\3\2\2\2\u0113\u0105\3\2\2\2\u0113\u0109\3\2\2\2\u0113\u010e"+
		"\3\2\2\2\u0114 \3\2\2\2\u0115\u0116\7F\2\2\u0116\u0117\7C\2\2\u0117\u0124"+
		"\7[\2\2\u0118\u0119\7f\2\2\u0119\u011a\7c\2\2\u011a\u0124\7{\2\2\u011b"+
		"\u011c\7f\2\2\u011c\u011d\7c\2\2\u011d\u011e\7{\2\2\u011e\u0124\7u\2\2"+
		"\u011f\u0120\7F\2\2\u0120\u0121\7C\2\2\u0121\u0122\7[\2\2\u0122\u0124"+
		"\7U\2\2\u0123\u0115\3\2\2\2\u0123\u0118\3\2\2\2\u0123\u011b\3\2\2\2\u0123"+
		"\u011f\3\2\2\2\u0124\"\3\2\2\2\u0125\u0126\7O\2\2\u0126\u0127\7Q\2\2\u0127"+
		"\u0128\7P\2\2\u0128\u0129\7V\2\2\u0129\u013c\7J\2\2\u012a\u012b\7o\2\2"+
		"\u012b\u012c\7q\2\2\u012c\u012d\7p\2\2\u012d\u012e\7v\2\2\u012e\u013c"+
		"\7j\2\2\u012f\u0130\7o\2\2\u0130\u0131\7q\2\2\u0131\u0132\7p\2\2\u0132"+
		"\u0133\7v\2\2\u0133\u0134\7j\2\2\u0134\u013c\7u\2\2\u0135\u0136\7O\2\2"+
		"\u0136\u0137\7Q\2\2\u0137\u0138\7P\2\2\u0138\u0139\7V\2\2\u0139\u013a"+
		"\7J\2\2\u013a\u013c\7U\2\2\u013b\u0125\3\2\2\2\u013b\u012a\3\2\2\2\u013b"+
		"\u012f\3\2\2\2\u013b\u0135\3\2\2\2\u013c$\3\2\2\2\u013d\u013e\7O\2\2\u013e"+
		"\u013f\7Q\2\2\u013f\u0140\7P\2\2\u0140\u0141\7F\2\2\u0141\u0142\7C\2\2"+
		"\u0142\u014a\7[\2\2\u0143\u0144\7o\2\2\u0144\u0145\7q\2\2\u0145\u0146"+
		"\7p\2\2\u0146\u0147\7f\2\2\u0147\u0148\7c\2\2\u0148\u014a\7{\2\2\u0149"+
		"\u013d\3\2\2\2\u0149\u0143\3\2\2\2\u014a&\3\2\2\2\u014b\u014c\7V\2\2\u014c"+
		"\u014d\7W\2\2\u014d\u014e\7G\2\2\u014e\u014f\7U\2\2\u014f\u0150\7F\2\2"+
		"\u0150\u0151\7C\2\2\u0151\u015a\7[\2\2\u0152\u0153\7v\2\2\u0153\u0154"+
		"\7w\2\2\u0154\u0155\7g\2\2\u0155\u0156\7u\2\2\u0156\u0157\7f\2\2\u0157"+
		"\u0158\7c\2\2\u0158\u015a\7{\2\2\u0159\u014b\3\2\2\2\u0159\u0152\3\2\2"+
		"\2\u015a(\3\2\2\2\u015b\u015c\7Y\2\2\u015c\u015d\7G\2\2\u015d\u015e\7"+
		"F\2\2\u015e\u015f\7P\2\2\u015f\u0160\7G\2\2\u0160\u0161\7U\2\2\u0161\u0162"+
		"\7F\2\2\u0162\u0163\7C\2\2\u0163\u016e\7[\2\2\u0164\u0165\7y\2\2\u0165"+
		"\u0166\7g\2\2\u0166\u0167\7f\2\2\u0167\u0168\7p\2\2\u0168\u0169\7g\2\2"+
		"\u0169\u016a\7u\2\2\u016a\u016b\7f\2\2\u016b\u016c\7c\2\2\u016c\u016e"+
		"\7{\2\2\u016d\u015b\3\2\2\2\u016d\u0164\3\2\2\2\u016e*\3\2\2\2\u016f\u0170"+
		"\7V\2\2\u0170\u0171\7J\2\2\u0171\u0172\7W\2\2\u0172\u0173\7T\2\2\u0173"+
		"\u0174\7U\2\2\u0174\u0175\7F\2\2\u0175\u0176\7C\2\2\u0176\u0180\7[\2\2"+
		"\u0177\u0178\7v\2\2\u0178\u0179\7j\2\2\u0179\u017a\7w\2\2\u017a\u017b"+
		"\7t\2\2\u017b\u017c\7u\2\2\u017c\u017d\7f\2\2\u017d\u017e\7c\2\2\u017e"+
		"\u0180\7{\2\2\u017f\u016f\3\2\2\2\u017f\u0177\3\2\2\2\u0180,\3\2\2\2\u0181"+
		"\u0182\7H\2\2\u0182\u0183\7T\2\2\u0183\u0184\7K\2\2\u0184\u0185\7F\2\2"+
		"\u0185\u0186\7C\2\2\u0186\u018e\7[\2\2\u0187\u0188\7h\2\2\u0188\u0189"+
		"\7t\2\2\u0189\u018a\7k\2\2\u018a\u018b\7f\2\2\u018b\u018c\7c\2\2\u018c"+
		"\u018e\7{\2\2\u018d\u0181\3\2\2\2\u018d\u0187\3\2\2\2\u018e.\3\2\2\2\u018f"+
		"\u0190\7U\2\2\u0190\u0191\7C\2\2\u0191\u0192\7V\2\2\u0192\u0193\7W\2\2"+
		"\u0193\u0194\7T\2\2\u0194\u0195\7F\2\2\u0195\u0196\7C\2\2\u0196\u01a0"+
		"\7[\2\2\u0197\u0198\7u\2\2\u0198\u0199\7c\2\2\u0199\u019a\7v\2\2\u019a"+
		"\u019b\7w\2\2\u019b\u019c\7t\2\2\u019c\u019d\7f\2\2\u019d\u019e\7c\2\2"+
		"\u019e\u01a0\7{\2\2\u019f\u018f\3\2\2\2\u019f\u0197\3\2\2\2\u01a0\60\3"+
		"\2\2\2\u01a1\u01a2\7U\2\2\u01a2\u01a3\7W\2\2\u01a3\u01a4\7P\2\2\u01a4"+
		"\u01a5\7F\2\2\u01a5\u01a6\7C\2\2\u01a6\u01ae\7[\2\2\u01a7\u01a8\7u\2\2"+
		"\u01a8\u01a9\7w\2\2\u01a9\u01aa\7p\2\2\u01aa\u01ab\7f\2\2\u01ab\u01ac"+
		"\7c\2\2\u01ac\u01ae\7{\2\2\u01ad\u01a1\3\2\2\2\u01ad\u01a7\3\2\2\2\u01ae"+
		"\62\3\2\2\2\u01af\u01b0\7v\2\2\u01b0\u01b1\7j\2\2\u01b1\u01b2\7k\2\2\u01b2"+
		"\u01b3\7u\2\2\u01b3\u01b4\7\"\2\2\u01b4\u01b5\7f\2\2\u01b5\u01b6\7c\2"+
		"\2\u01b6\u01b7\7{\2\2\u01b7\u01b8\7\"\2\2\u01b8\u01b9\7q\2\2\u01b9\u01ba"+
		"\7h\2\2\u01ba\u01bb\7\"\2\2\u01bb\u01bc\7y\2\2\u01bc\u01bd\7g\2\2\u01bd"+
		"\u01be\7g\2\2\u01be\u01d0\7m\2\2\u01bf\u01c0\7V\2\2\u01c0\u01c1\7J\2\2"+
		"\u01c1\u01c2\7K\2\2\u01c2\u01c3\7U\2\2\u01c3\u01c4\7\"\2\2\u01c4\u01c5"+
		"\7F\2\2\u01c5\u01c6\7C\2\2\u01c6\u01c7\7[\2\2\u01c7\u01c8\7\"\2\2\u01c8"+
		"\u01c9\7Q\2\2\u01c9\u01ca\7H\2\2\u01ca\u01cb\7\"\2\2\u01cb\u01cc\7Y\2"+
		"\2\u01cc\u01cd\7G\2\2\u01cd\u01ce\7G\2\2\u01ce\u01d0\7M\2\2\u01cf\u01af"+
		"\3\2\2\2\u01cf\u01bf\3\2\2\2\u01d0\64\3\2\2\2\u01d1\u01d2\7y\2\2\u01d2"+
		"\u01d3\7g\2\2\u01d3\u01d4\7g\2\2\u01d4\u01d5\7m\2\2\u01d5\u01d6\7g\2\2"+
		"\u01d6\u01d7\7p\2\2\u01d7\u01f0\7f\2\2\u01d8\u01d9\7Y\2\2\u01d9\u01da"+
		"\7G\2\2\u01da\u01db\7G\2\2\u01db\u01dc\7M\2\2\u01dc\u01dd\7G\2\2\u01dd"+
		"\u01de\7P\2\2\u01de\u01f0\7F\2\2\u01df\u01e0\7y\2\2\u01e0\u01e1\7g\2\2"+
		"\u01e1\u01e2\7g\2\2\u01e2\u01e3\7m\2\2\u01e3\u01e4\7g\2\2\u01e4\u01e5"+
		"\7p\2\2\u01e5\u01e6\7f\2\2\u01e6\u01f0\7u\2\2\u01e7\u01e8\7Y\2\2\u01e8"+
		"\u01e9\7G\2\2\u01e9\u01ea\7G\2\2\u01ea\u01eb\7M\2\2\u01eb\u01ec\7G\2\2"+
		"\u01ec\u01ed\7P\2\2\u01ed\u01ee\7F\2\2\u01ee\u01f0\7U\2\2\u01ef\u01d1"+
		"\3\2\2\2\u01ef\u01d8\3\2\2\2\u01ef\u01df\3\2\2\2\u01ef\u01e7\3\2\2\2\u01f0"+
		"\66\3\2\2\2\u01f1\u01f2\7y\2\2\u01f2\u01f3\7g\2\2\u01f3\u01f4\7g\2\2\u01f4"+
		"\u01f5\7m\2\2\u01f5\u01f6\7f\2\2\u01f6\u01f7\7c\2\2\u01f7\u0210\7{\2\2"+
		"\u01f8\u01f9\7Y\2\2\u01f9\u01fa\7G\2\2\u01fa\u01fb\7G\2\2\u01fb\u01fc"+
		"\7M\2\2\u01fc\u01fd\7F\2\2\u01fd\u01fe\7C\2\2\u01fe\u0210\7[\2\2\u01ff"+
		"\u0200\7y\2\2\u0200\u0201\7g\2\2\u0201\u0202\7g\2\2\u0202\u0203\7m\2\2"+
		"\u0203\u0204\7f\2\2\u0204\u0205\7c\2\2\u0205\u0206\7{\2\2\u0206\u0210"+
		"\7u\2\2\u0207\u0208\7Y\2\2\u0208\u0209\7G\2\2\u0209\u020a\7G\2\2\u020a"+
		"\u020b\7M\2\2\u020b\u020c\7F\2\2\u020c\u020d\7C\2\2\u020d\u020e\7[\2\2"+
		"\u020e\u0210\7U\2\2\u020f\u01f1\3\2\2\2\u020f\u01f8\3\2\2\2\u020f\u01ff"+
		"\3\2\2\2\u020f\u0207\3\2\2\2\u02108\3\2\2\2\u0211\u0212\4\62;\2\u0212"+
		":\3\2\2\2\u0213\u0214\4\63;\2\u0214<\3\2\2\2\35\2Mm\u008d\u0095\u009f"+
		"\u00ab\u00bb\u00c3\u00c9\u00cf\u00dd\u00e3\u00ff\u0113\u0123\u013b\u0149"+
		"\u0159\u016d\u017f\u018d\u019f\u01ad\u01cf\u01ef\u020f\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}