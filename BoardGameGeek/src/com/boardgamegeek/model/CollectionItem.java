package com.boardgamegeek.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

import android.text.TextUtils;

import com.boardgamegeek.model.Game.Rank;
import com.boardgamegeek.util.DateTimeUtils;
import com.boardgamegeek.util.StringUtils;

@Root(name = "item")
public class CollectionItem {
	private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

	private long mLastModifiedDate = DateTimeUtils.UNPARSED_DATE;

	public static class Statistics {
		@Attribute(required = false)
		public int minplayers;

		@Attribute(required = false)
		public int maxplayers;

		@Attribute(required = false)
		public int playingtime;

		@Attribute
		public int numowned;

		@Path("rating")
		@Attribute(name = "value")
		private String rating;

		public double getRating() {
			return StringUtils.parseDouble(rating, 0.0);
		}

		@Path("rating/usersrated")
		@Attribute(name = "value")
		private int usersRated;

		@Path("rating/average")
		@Attribute(name = "value")
		private double average;

		@Path("rating/bayesaverage")
		@Attribute(name = "value")
		private double bayesAverage;

		@Path("rating/stddev")
		@Attribute(name = "value")
		private double standardDeviation;

		@Path("rating/median")
		@Attribute(name = "value")
		private double median;

		@Path("rating")
		@ElementList
		private List<Rank> ranks;
	}

	// "thing"
	@Attribute
	private String objecttype;

	@Attribute(name = "objectid")
	public int gameId;

	/**
	 * Includes boardgame, boardgameexpansion, videogame, rpg, rpgitem
	 */
	@Attribute
	private String subtype;

	@Attribute(name = "collid", empty = "-1")
	public int collectionId;

	@Path("name")
	@Text
	private String name;

	@Element(required = false)
	private String originalname;

	@Path("name")
	@Attribute
	private int sortindex;

	@Element(required = false)
	public int yearpublished;

	@Path("status")
	@Attribute
	public int own;

	@Path("status")
	@Attribute
	public int prevowned;

	@Path("status")
	@Attribute
	public int fortrade;

	@Path("status")
	@Attribute
	public int want;

	@Path("status")
	@Attribute
	public int wanttoplay;

	@Path("status")
	@Attribute
	public int wanttobuy;

	@Path("status")
	@Attribute
	public int wishlist;

	@Path("status")
	@Attribute(required = false)
	public int wishlistpriority;

	@Path("status")
	@Attribute
	public int preordered;

	@Path("status")
	@Attribute
	private String lastmodified;

	@Element(required = false)
	public String image;

	@Element(required = false)
	public String thumbnail;

	@Element(name = "stats", required = false)
	public Statistics statistics;

	@Element(required = false)
	public int numplays;

	@Path("privateinfo")
	@Attribute(name = "pp_currency", required = false)
	public String pricePaidCurrency;

	@Path("privateinfo")
	@Attribute(required = false, empty = "0.0")
	private String pricepaid;

	public double pricePaid() {
		try {
			return Double.parseDouble(pricepaid);
		} catch (NumberFormatException | NullPointerException e) {
			return 0.0;
		}
	}

	@Path("privateinfo")
	@Attribute(name = "cv_currency", required = false)
	public String currentValueCurrency;

	@Path("privateinfo")
	@Attribute(required = false, empty = "0.0")
	private String currvalue;

	public double currentValue() {
		try {
			return Double.parseDouble(currvalue);
		} catch (NumberFormatException | NullPointerException e) {
			return 0.0;
		}
	}

	@Path("privateinfo")
	@Attribute(required = false)
	public int quantity;

	@Path("privateinfo")
	@Attribute(name = "acquisitiondate", required = false)
	public String acquisitionDate; // "2000-12-08"

	@Path("privateinfo")
	@Attribute(name = "acquiredfrom", required = false)
	public String acquiredFrom;

	@Path("privateinfo")
	@Element(required = false)
	public String privatecomment;

	@Element(required = false)
	public String comment;

	@Element(required = false)
	public String conditiontext;

	@Element(required = false)
	public String wantpartslist;

	@Element(required = false)
	public String haspartslist;

	@Element(required = false)
	public String wishlistcomment;

	public String collectionName() {
		return name;
	}

	public String collectionSortName() {
		if (TextUtils.isEmpty(originalname)) {
			return StringUtils.createSortName(name, sortindex);
		} else {
			return name;
		}
	}

	public String gameName() {
		if (TextUtils.isEmpty(originalname)) {
			return name;
		} else {
			return originalname;
		}
	}

	public String gameSortName() {
		if (TextUtils.isEmpty(originalname)) {
			return StringUtils.createSortName(name, sortindex);
		} else {
			return StringUtils.createSortName(originalname, sortindex);
		}
	}

	public long lastModifiedDate() {
		mLastModifiedDate = DateTimeUtils.tryParseDate(mLastModifiedDate, lastmodified, FORMAT);
		return mLastModifiedDate;
	}
}
