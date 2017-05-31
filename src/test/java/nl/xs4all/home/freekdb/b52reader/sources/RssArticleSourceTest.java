/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.sources;

import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndCategoryImpl;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import nl.xs4all.home.freekdb.b52reader.general.ObjectHub;
import nl.xs4all.home.freekdb.b52reader.model.Article;
import nl.xs4all.home.freekdb.b52reader.model.Author;
import nl.xs4all.home.freekdb.b52reader.model.database.PersistencyHandler;
import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RssArticleSourceTest {
    private static final String SOURCE_ID = "test-source-id";
    private static final String FEED_NAME = "Some rss feed";
    private static final Author TEST_AUTHOR_1 = new Author(6, "Test Author");
    private static final Author TEST_AUTHOR_2 = new Author(28, "Test Author II");
    private static final String CATEGORY_NAME = "category-name";
    private static final String ARTICLE_TITLE_1 = "entry-title-1";
    private static final String ARTICLE_TITLE_2 = "entry-title-2";
    private static final String ARTICLE_TEXT = "text-1";

    private URL feedUrl;

    @Before
    public void setUp() throws MalformedURLException {
        feedUrl = new URL("https://test.org");
    }

    @Test
    public void testGetters() {
        RssArticleSource rssArticleSource = new RssArticleSource(SOURCE_ID, null, FEED_NAME, TEST_AUTHOR_1,
                                                                 feedUrl, null);

        assertEquals(SOURCE_ID, rssArticleSource.getSourceId());
        assertEquals(FEED_NAME, rssArticleSource.getFeedName());
        assertEquals(TEST_AUTHOR_1, rssArticleSource.getDefaultAuthor());
        assertEquals(feedUrl, rssArticleSource.getFeedUrl());
        assertNull(rssArticleSource.getCategoryName());
    }

    @Test
    public void testGetArticlesEmpty() {
        SyndFeed mockFeed = Mockito.mock(SyndFeed.class);
        Mockito.when(mockFeed.getEntries()).thenReturn(new ArrayList<>());

        RssArticleSource rssArticleSource = new RssArticleSource(SOURCE_ID, mockFeed, FEED_NAME, TEST_AUTHOR_1, feedUrl,
                                                                 null);

        assertEquals(0, rssArticleSource.getArticles(null, null).size());
    }

    @Test
    public void testGetArticlesCategoryNull() {
        RssArticleSource rssArticleSource = new RssArticleSource(SOURCE_ID, createMockFeed(false), FEED_NAME,
                                                                 TEST_AUTHOR_1, feedUrl, null);

        List<Article> actualArticles = rssArticleSource.getArticles(new HashMap<>(), new HashMap<>());

        assertEquals(prepareExpectedArticles(TEST_AUTHOR_1, true, actualArticles), actualArticles);
    }

    @Test
    public void testGetArticlesCategoryNamed() {
        // The default author is TEST_AUTHOR_1, but the mock feed should provide the name of TEST_AUTHOR_2.
        RssArticleSource rssArticleSource = new RssArticleSource(SOURCE_ID, createMockFeed(true), FEED_NAME,
                                                                 TEST_AUTHOR_1, feedUrl, CATEGORY_NAME);

        List<Article> actualArticles = rssArticleSource.getArticles(new HashMap<>(), new HashMap<>());

        assertEquals(prepareExpectedArticles(TEST_AUTHOR_2, false, actualArticles), actualArticles);
    }

    private SyndFeed createMockFeed(boolean addExtraFields) {
        PersistencyHandler mockPersistencyHandler = Mockito.mock(PersistencyHandler.class);
        Mockito.when(mockPersistencyHandler.getOrCreateAuthor(TEST_AUTHOR_2.getName())).thenReturn(TEST_AUTHOR_2);
        ObjectHub.injectPersistencyHandler(mockPersistencyHandler);

        SyndFeed mockFeed = Mockito.mock(SyndFeed.class);
        SyndEntry mockEntry1 = Mockito.mock(SyndEntry.class);
        SyndEntry mockEntry2 = Mockito.mock(SyndEntry.class);

        SyndContent description = new SyndContentImpl();
        description.setValue(ARTICLE_TEXT);

        Mockito.when(mockFeed.getEntries()).thenReturn(Arrays.asList(mockEntry1, mockEntry2));
        Mockito.when(mockEntry1.getTitle()).thenReturn(ARTICLE_TITLE_1);
        Mockito.when(mockEntry2.getTitle()).thenReturn(ARTICLE_TITLE_2);
        Mockito.when(mockEntry1.getDescription()).thenReturn(description);

        if (addExtraFields) {
            SyndCategory category = new SyndCategoryImpl();
            category.setName(CATEGORY_NAME);
            Date date = Utilities.createDate(1882, Month.JUNE, 28);

            Mockito.when(mockEntry1.getCategories()).thenReturn(Collections.singletonList(category));
            Mockito.when(mockEntry1.getAuthor()).thenReturn(TEST_AUTHOR_2.getName());
            Mockito.when(mockEntry1.getPublishedDate()).thenReturn(date);
        }

        return mockFeed;
    }

    // todo: Pass a Clock object to the RssArticleSource class instead of copying date/times here.
    private List<Article> prepareExpectedArticles(Author testAuthor, boolean expectTwoArticles,
                                                  List<Article> actualArticles) {
        List<Article> expectedArticles = new ArrayList<>();

        expectedArticles.add(new Article(null, null, testAuthor, ARTICLE_TITLE_1,
                                         actualArticles.get(0).getDateTime(), ARTICLE_TEXT, 1234, -1));

        if (expectTwoArticles) {
            expectedArticles.add(new Article(null, null, testAuthor, ARTICLE_TITLE_2,
                                             actualArticles.get(1).getDateTime(), "", 1234, -2));
        }

        return expectedArticles;
    }
}
