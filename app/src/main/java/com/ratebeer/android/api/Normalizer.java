package com.ratebeer.android.api;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class Normalizer {

	private final Map<String, Integer> HTML_ENTITIES = new HashMap<>();
	private final DateFormat ASP_DATE_FORMAT = new SimpleDateFormat("M/d/yyyy K:m:s a", Locale.US);

	private static class Holder {
		// Holder with static instance which implements a thread safe lazy loading singleton
		static final Normalizer INSTANCE = new Normalizer();
	}

	public static Normalizer get() {
		return Holder.INSTANCE;
	}

	public String cleanHtml(String raw) {
		return cleanHtml(raw, false);
	}

	public String cleanHtml(String raw, boolean trim) {
		// Translation of HTML-encoded characters (and line breaks)
		raw = raw.replaceAll("\r\n", "");
		raw = raw.replaceAll("\n", "");
		raw = raw.replaceAll("\r", "");
		raw = raw.replaceAll("<br>", "\n");
		raw = raw.replaceAll("<br />", "\n");
		raw = raw.replaceAll("<BR>", "\n");
		raw = raw.replaceAll("<BR />", "\n");
		raw = raw.replaceAll("&quot;", "\"");
		if (trim)
			raw = raw.trim();
		return unHtmlEntities(raw);
	}

	public String normalizeSearchQuery(String query) {
		// RateBeer crashes down badly when providing a ' (apostrophe) in a search; replace it instead by a ? (wildcard)
		query = query.replace("'", "?");

		// Translates diacritics
		// (from http://stackoverflow.com/questions/1008802/converting-symbols-accent-letters-to-english-alphabet)
		String normalized = java.text.Normalizer.normalize(query, java.text.Normalizer.Form.NFD);

		// Remove the marks to only leave Latin characters
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		query = pattern.matcher(normalized).replaceAll("");

		// Translate other special characters into English alphabet characters by hand
		query = query.replaceAll("æ", "ae");
		query = query.replaceAll("Æ", "AE");
		query = query.replaceAll("ß", "ss");
		query = query.replaceAll("ø", "o");
		query = query.replaceAll("Ø", "O");
		return query.trim();

	}

	public Date parseTime(String raw) {
		try {
			return ASP_DATE_FORMAT.parse(raw);
		} catch (ParseException e) {
			// Unexpected format; just ignore and return null
			return null;
		}
	}

	/**
	 * Convert HTML entities to special and extended unicode characters equivalents.<br/><br/> Copyright (c) 2004-2005 Tecnick.com S.r.l
	 * (www.tecnick.com) Via Ugo Foscolo n.19 - 09045 Quartu Sant'Elena (CA) - ITALY - www.tecnick.com - info@tecnick.com<br/> Author: Nicola Asuni
	 * [www.tecnick.com] Project homepage: <a href="http://htmlentities.sourceforge.net" target="_blank">http://htmlentities.sourceforge.net</a><br/>
	 * License: http://www.gnu.org/copyleft/lesser.html LGPL
	 * @param string Raw input string, which can contain
	 * @return formatted string
	 */
	private String unHtmlEntities(String string) {

		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < string.length(); ++i) {
			char ch = string.charAt(i);
			if (ch == '&') {
				int semi = string.indexOf(';', i + 1);
				if ((semi == -1) || ((semi - i) > 7)) {
					buf.append(ch);
					continue;
				}
				String entity = string.substring(i, semi + 1);
				Integer iso;
				if (entity.charAt(1) == ' ') {
					buf.append(ch);
					continue;
				}
				if (entity.charAt(1) == '#') {
					if (entity.charAt(2) == 'x') {
						iso = Integer.parseInt(entity.substring(3, entity.length() - 1), 16);
					} else {
						iso = Integer.valueOf(entity.substring(2, entity.length() - 1));
					}
				} else {
					iso = HTML_ENTITIES.get(entity);
				}
				if (iso == null) {
					buf.append(entity);
				} else {
					buf.append((char) (iso.intValue()));
				}
				i = semi;
			} else {
				buf.append(ch);
			}
		}
		return buf.toString();

	}

	/**
	 * Translation table for HTML entities.<br/> reference: W3C - Character entity references in HTML 4 [<a
	 * href="http://www.w3.org/TR/html401/sgml/entities.html" target="_blank">http://www.w3.org/TR/html401/sgml/entities.html</a>].
	 */ {
		HTML_ENTITIES.put("&Aacute;", 193);
		HTML_ENTITIES.put("&aacute;", 225);
		HTML_ENTITIES.put("&Acirc;", 194);
		HTML_ENTITIES.put("&acirc;", 226);
		HTML_ENTITIES.put("&acute;", 180);
		HTML_ENTITIES.put("&AElig;", 198);
		HTML_ENTITIES.put("&aelig;", 230);
		HTML_ENTITIES.put("&Agrave;", 192);
		HTML_ENTITIES.put("&agrave;", 224);
		HTML_ENTITIES.put("&alefsym;", 8501);
		HTML_ENTITIES.put("&Alpha;", 913);
		HTML_ENTITIES.put("&alpha;", 945);
		HTML_ENTITIES.put("&amp;", 38);
		HTML_ENTITIES.put("&and;", 8743);
		HTML_ENTITIES.put("&ang;", 8736);
		HTML_ENTITIES.put("&Aring;", 197);
		HTML_ENTITIES.put("&aring;", 229);
		HTML_ENTITIES.put("&asymp;", 8776);
		HTML_ENTITIES.put("&Atilde;", 195);
		HTML_ENTITIES.put("&atilde;", 227);
		HTML_ENTITIES.put("&Auml;", 196);
		HTML_ENTITIES.put("&auml;", 228);
		HTML_ENTITIES.put("&bdquo;", 8222);
		HTML_ENTITIES.put("&Beta;", 914);
		HTML_ENTITIES.put("&beta;", 946);
		HTML_ENTITIES.put("&brvbar;", 166);
		HTML_ENTITIES.put("&bull;", 8226);
		HTML_ENTITIES.put("&cap;", 8745);
		HTML_ENTITIES.put("&Ccedil;", 199);
		HTML_ENTITIES.put("&ccedil;", 231);
		HTML_ENTITIES.put("&cedil;", 184);
		HTML_ENTITIES.put("&cent;", 162);
		HTML_ENTITIES.put("&Chi;", 935);
		HTML_ENTITIES.put("&chi;", 967);
		HTML_ENTITIES.put("&circ;", 710);
		HTML_ENTITIES.put("&clubs;", 9827);
		HTML_ENTITIES.put("&cong;", 8773);
		HTML_ENTITIES.put("&copy;", 169);
		HTML_ENTITIES.put("&crarr;", 8629);
		HTML_ENTITIES.put("&cup;", 8746);
		HTML_ENTITIES.put("&curren;", 164);
		HTML_ENTITIES.put("&dagger;", 8224);
		HTML_ENTITIES.put("&Dagger;", 8225);
		HTML_ENTITIES.put("&darr;", 8595);
		HTML_ENTITIES.put("&dArr;", 8659);
		HTML_ENTITIES.put("&deg;", 176);
		HTML_ENTITIES.put("&Delta;", 916);
		HTML_ENTITIES.put("&delta;", 948);
		HTML_ENTITIES.put("&diams;", 9830);
		HTML_ENTITIES.put("&divide;", 247);
		HTML_ENTITIES.put("&Eacute;", 201);
		HTML_ENTITIES.put("&eacute;", 233);
		HTML_ENTITIES.put("&Ecirc;", 202);
		HTML_ENTITIES.put("&ecirc;", 234);
		HTML_ENTITIES.put("&Egrave;", 200);
		HTML_ENTITIES.put("&egrave;", 232);
		HTML_ENTITIES.put("&empty;", 8709);
		HTML_ENTITIES.put("&emsp;", 8195);
		HTML_ENTITIES.put("&ensp;", 8194);
		HTML_ENTITIES.put("&Epsilon;", 917);
		HTML_ENTITIES.put("&epsilon;", 949);
		HTML_ENTITIES.put("&equiv;", 8801);
		HTML_ENTITIES.put("&Eta;", 919);
		HTML_ENTITIES.put("&eta;", 951);
		HTML_ENTITIES.put("&ETH;", 208);
		HTML_ENTITIES.put("&eth;", 240);
		HTML_ENTITIES.put("&Euml;", 203);
		HTML_ENTITIES.put("&euml;", 235);
		HTML_ENTITIES.put("&euro;", 8364);
		HTML_ENTITIES.put("&exist;", 8707);
		HTML_ENTITIES.put("&fnof;", 402);
		HTML_ENTITIES.put("&forall;", 8704);
		HTML_ENTITIES.put("&frac12;", 189);
		HTML_ENTITIES.put("&frac14;", 188);
		HTML_ENTITIES.put("&frac34;", 190);
		HTML_ENTITIES.put("&frasl;", 8260);
		HTML_ENTITIES.put("&Gamma;", 915);
		HTML_ENTITIES.put("&gamma;", 947);
		HTML_ENTITIES.put("&ge;", 8805);
		HTML_ENTITIES.put("&harr;", 8596);
		HTML_ENTITIES.put("&hArr;", 8660);
		HTML_ENTITIES.put("&hearts;", 9829);
		HTML_ENTITIES.put("&hellip;", 8230);
		HTML_ENTITIES.put("&Iacute;", 205);
		HTML_ENTITIES.put("&iacute;", 237);
		HTML_ENTITIES.put("&Icirc;", 206);
		HTML_ENTITIES.put("&icirc;", 238);
		HTML_ENTITIES.put("&iexcl;", 161);
		HTML_ENTITIES.put("&Igrave;", 204);
		HTML_ENTITIES.put("&igrave;", 236);
		HTML_ENTITIES.put("&image;", 8465);
		HTML_ENTITIES.put("&infin;", 8734);
		HTML_ENTITIES.put("&int;", 8747);
		HTML_ENTITIES.put("&Iota;", 921);
		HTML_ENTITIES.put("&iota;", 953);
		HTML_ENTITIES.put("&iquest;", 191);
		HTML_ENTITIES.put("&isin;", 8712);
		HTML_ENTITIES.put("&Iuml;", 207);
		HTML_ENTITIES.put("&iuml;", 239);
		HTML_ENTITIES.put("&Kappa;", 922);
		HTML_ENTITIES.put("&kappa;", 954);
		HTML_ENTITIES.put("&Lambda;", 923);
		HTML_ENTITIES.put("&lambda;", 955);
		HTML_ENTITIES.put("&lang;", 9001);
		HTML_ENTITIES.put("&laquo;", 171);
		HTML_ENTITIES.put("&larr;", 8592);
		HTML_ENTITIES.put("&lArr;", 8656);
		HTML_ENTITIES.put("&lceil;", 8968);
		HTML_ENTITIES.put("&ldquo;", 8220);
		HTML_ENTITIES.put("&le;", 8804);
		HTML_ENTITIES.put("&lfloor;", 8970);
		HTML_ENTITIES.put("&lowast;", 8727);
		HTML_ENTITIES.put("&loz;", 9674);
		HTML_ENTITIES.put("&lrm;", 8206);
		HTML_ENTITIES.put("&lsaquo;", 8249);
		HTML_ENTITIES.put("&lsquo;", 8216);
		HTML_ENTITIES.put("&macr;", 175);
		HTML_ENTITIES.put("&mdash;", 8212);
		HTML_ENTITIES.put("&micro;", 181);
		HTML_ENTITIES.put("&middot;", 183);
		HTML_ENTITIES.put("&minus;", 8722);
		HTML_ENTITIES.put("&Mu;", 924);
		HTML_ENTITIES.put("&mu;", 956);
		HTML_ENTITIES.put("&nabla;", 8711);
		HTML_ENTITIES.put("&nbsp;", 160);
		HTML_ENTITIES.put("&ndash;", 8211);
		HTML_ENTITIES.put("&ne;", 8800);
		HTML_ENTITIES.put("&ni;", 8715);
		HTML_ENTITIES.put("&not;", 172);
		HTML_ENTITIES.put("&notin;", 8713);
		HTML_ENTITIES.put("&nsub;", 8836);
		HTML_ENTITIES.put("&Ntilde;", 209);
		HTML_ENTITIES.put("&ntilde;", 241);
		HTML_ENTITIES.put("&Nu;", 925);
		HTML_ENTITIES.put("&nu;", 957);
		HTML_ENTITIES.put("&Oacute;", 211);
		HTML_ENTITIES.put("&oacute;", 243);
		HTML_ENTITIES.put("&Ocirc;", 212);
		HTML_ENTITIES.put("&ocirc;", 244);
		HTML_ENTITIES.put("&OElig;", 338);
		HTML_ENTITIES.put("&oelig;", 339);
		HTML_ENTITIES.put("&Ograve;", 210);
		HTML_ENTITIES.put("&ograve;", 242);
		HTML_ENTITIES.put("&oline;", 8254);
		HTML_ENTITIES.put("&Omega;", 937);
		HTML_ENTITIES.put("&omega;", 969);
		HTML_ENTITIES.put("&Omicron;", 927);
		HTML_ENTITIES.put("&omicron;", 959);
		HTML_ENTITIES.put("&oplus;", 8853);
		HTML_ENTITIES.put("&or;", 8744);
		HTML_ENTITIES.put("&ordf;", 170);
		HTML_ENTITIES.put("&ordm;", 186);
		HTML_ENTITIES.put("&Oslash;", 216);
		HTML_ENTITIES.put("&oslash;", 248);
		HTML_ENTITIES.put("&Otilde;", 213);
		HTML_ENTITIES.put("&otilde;", 245);
		HTML_ENTITIES.put("&otimes;", 8855);
		HTML_ENTITIES.put("&Ouml;", 214);
		HTML_ENTITIES.put("&ouml;", 246);
		HTML_ENTITIES.put("&para;", 182);
		HTML_ENTITIES.put("&part;", 8706);
		HTML_ENTITIES.put("&permil;", 8240);
		HTML_ENTITIES.put("&perp;", 8869);
		HTML_ENTITIES.put("&Phi;", 934);
		HTML_ENTITIES.put("&phi;", 966);
		HTML_ENTITIES.put("&Pi;", 928);
		HTML_ENTITIES.put("&pi;", 960);
		HTML_ENTITIES.put("&piv;", 982);
		HTML_ENTITIES.put("&plusmn;", 177);
		HTML_ENTITIES.put("&pound;", 163);
		HTML_ENTITIES.put("&prime;", 8242);
		HTML_ENTITIES.put("&Prime;", 8243);
		HTML_ENTITIES.put("&prod;", 8719);
		HTML_ENTITIES.put("&prop;", 8733);
		HTML_ENTITIES.put("&Psi;", 936);
		HTML_ENTITIES.put("&psi;", 968);
		HTML_ENTITIES.put("&radic;", 8730);
		HTML_ENTITIES.put("&rang;", 9002);
		HTML_ENTITIES.put("&raquo;", 187);
		HTML_ENTITIES.put("&rarr;", 8594);
		HTML_ENTITIES.put("&rArr;", 8658);
		HTML_ENTITIES.put("&rceil;", 8969);
		HTML_ENTITIES.put("&rdquo;", 8221);
		HTML_ENTITIES.put("&real;", 8476);
		HTML_ENTITIES.put("&reg;", 174);
		HTML_ENTITIES.put("&rfloor;", 8971);
		HTML_ENTITIES.put("&Rho;", 929);
		HTML_ENTITIES.put("&rho;", 961);
		HTML_ENTITIES.put("&rlm;", 8207);
		HTML_ENTITIES.put("&rsaquo;", 8250);
		HTML_ENTITIES.put("&rsquo;", 8217);
		HTML_ENTITIES.put("&sbquo;", 8218);
		HTML_ENTITIES.put("&Scaron;", 352);
		HTML_ENTITIES.put("&scaron;", 353);
		HTML_ENTITIES.put("&sdot;", 8901);
		HTML_ENTITIES.put("&sect;", 167);
		HTML_ENTITIES.put("&shy;", 173);
		HTML_ENTITIES.put("&Sigma;", 931);
		HTML_ENTITIES.put("&sigma;", 963);
		HTML_ENTITIES.put("&sigmaf;", 962);
		HTML_ENTITIES.put("&sim;", 8764);
		HTML_ENTITIES.put("&spades;", 9824);
		HTML_ENTITIES.put("&sub;", 8834);
		HTML_ENTITIES.put("&sube;", 8838);
		HTML_ENTITIES.put("&sum;", 8721);
		HTML_ENTITIES.put("&sup1;", 185);
		HTML_ENTITIES.put("&sup2;", 178);
		HTML_ENTITIES.put("&sup3;", 179);
		HTML_ENTITIES.put("&sup;", 8835);
		HTML_ENTITIES.put("&supe;", 8839);
		HTML_ENTITIES.put("&szlig;", 223);
		HTML_ENTITIES.put("&Tau;", 932);
		HTML_ENTITIES.put("&tau;", 964);
		HTML_ENTITIES.put("&there4;", 8756);
		HTML_ENTITIES.put("&Theta;", 920);
		HTML_ENTITIES.put("&theta;", 952);
		HTML_ENTITIES.put("&thetasym;", 977);
		HTML_ENTITIES.put("&thinsp;", 8201);
		HTML_ENTITIES.put("&THORN;", 222);
		HTML_ENTITIES.put("&thorn;", 254);
		HTML_ENTITIES.put("&tilde;", 732);
		HTML_ENTITIES.put("&times;", 215);
		HTML_ENTITIES.put("&trade;", 8482);
		HTML_ENTITIES.put("&Uacute;", 218);
		HTML_ENTITIES.put("&uacute;", 250);
		HTML_ENTITIES.put("&uarr;", 8593);
		HTML_ENTITIES.put("&uArr;", 8657);
		HTML_ENTITIES.put("&Ucirc;", 219);
		HTML_ENTITIES.put("&ucirc;", 251);
		HTML_ENTITIES.put("&Ugrave;", 217);
		HTML_ENTITIES.put("&ugrave;", 249);
		HTML_ENTITIES.put("&uml;", 168);
		HTML_ENTITIES.put("&upsih;", 978);
		HTML_ENTITIES.put("&Upsilon;", 933);
		HTML_ENTITIES.put("&upsilon;", 965);
		HTML_ENTITIES.put("&Uuml;", 220);
		HTML_ENTITIES.put("&uuml;", 252);
		HTML_ENTITIES.put("&weierp;", 8472);
		HTML_ENTITIES.put("&Xi;", 926);
		HTML_ENTITIES.put("&xi;", 958);
		HTML_ENTITIES.put("&Yacute;", 221);
		HTML_ENTITIES.put("&yacute;", 253);
		HTML_ENTITIES.put("&yen;", 165);
		HTML_ENTITIES.put("&yuml;", 255);
		HTML_ENTITIES.put("&Yuml;", 376);
		HTML_ENTITIES.put("&Zeta;", 918);
		HTML_ENTITIES.put("&zeta;", 950);
		HTML_ENTITIES.put("&zwj;", 8205);
		HTML_ENTITIES.put("&zwnj;", 8204);
	}

}
