package org.icij.datashare.web;

import net.codestory.http.WebServer;
import net.codestory.http.misc.Env;
import net.codestory.rest.FluentRestTest;
import org.icij.datashare.Repository;
import org.icij.datashare.text.Document;
import org.icij.datashare.text.indexing.Indexer;
import org.icij.datashare.web.DocumentResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.icij.datashare.text.Language.FRENCH;
import static org.icij.datashare.text.Project.project;
import static org.icij.datashare.text.Tag.tag;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class DocumentResourceTest implements FluentRestTest {
    private static WebServer server = new WebServer() {
        @Override
        protected Env createEnv() {
            return Env.prod();
        }
    }.startOnRandomPort();
    @Mock
    Repository repository;
    @Mock Indexer indexer;

    @Before
    public void setUp() {
        initMocks(this);
        server.configure(routes -> routes.add(new DocumentResource(repository, indexer)));
    }

    @Test
    public void testStarDocument() throws Exception {
        when(repository.star(any(), any())).thenReturn(true).thenReturn(false);
        put("/api/document/star/doc_id").should().respond(201);
        put("/api/document/star/doc_id").should().respond(200);
    }

    @Test
    public void testUnstarDocument() throws Exception {
        when(repository.unstar(any(), any())).thenReturn(true).thenReturn(false);
        put("/api/document/unstar/doc_id").should().respond(201);
        put("/api/document/unstar/doc_id").should().respond(200);
    }

    @Test
    public void testStarDocumentWithProject() throws Exception {
        when(repository.star(any(), any(), eq("doc_id"))).thenReturn(true).thenReturn(false);
        put("/api/document/project/star/prj1/doc_id").should().respond(201);
        put("/api/document/project/star/prj1/doc_id").should().respond(200);
    }

    @Test
    public void testUnstarDocumentWithProject() throws Exception {
        when(repository.unstar(any(), any(), eq("doc_id"))).thenReturn(true).thenReturn(false);
        put("/api/document/project/unstar/prj2/doc_id").should().respond(201);
        put("/api/document/project/unstar/prj2/doc_id").should().respond(200);
    }

    @Test
    public void testTagDocumentWithProject() throws Exception {
        when(repository.tag(eq(project("prj1")), any(), eq(tag("tag1")), eq(tag("tag2")))).thenReturn(true).thenReturn(false);
        when(indexer.tag(eq(project("prj1")), any(), any(), eq(tag("tag1")), eq(tag("tag2")))).thenReturn(true).thenReturn(false);

        put("/api/document/project/tag/prj1/doc_id", "[\"tag1\", \"tag2\"]").should().respond(201);
        put("/api/document/project/tag/prj1/doc_id", "[\"tag1\", \"tag2\"]").should().respond(200);

        verify(indexer, times(2)).tag(eq(project("prj1")), any(), any(), eq(tag("tag1")), eq(tag("tag2")));
    }

    @Test
    public void testUntagDocumentWithProject() throws Exception {
        when(repository.untag(eq(project("prj2")), any(), eq(tag("tag3")), eq(tag("tag4")))).thenReturn(true).thenReturn(false);
        when(indexer.untag(eq(project("prj2")), any(), any(), eq(tag("tag3")), eq(tag("tag4")))).thenReturn(true).thenReturn(false);

        put("/api/document/project/untag/prj2/doc_id", "[\"tag3\", \"tag4\"]").should().respond(201);
        put("/api/document/project/untag/prj2/doc_id", "[\"tag3\", \"tag4\"]").should().respond(200);

        verify(indexer, times(2)).untag(eq(project("prj2")), any(), any(), any(), any());
    }

    @Test
    public void testGetTaggedDocumentsWithProject() throws Exception {
        when(repository.getDocuments(eq(project("prj3")), eq(tag("foo")), eq(tag("bar")), eq(tag("baz")))).thenReturn(asList("id1", "id2"));
        get("/api/document/project/tagged/prj3/foo,bar,baz").should().respond(200).contain("id1").contain("id2");
    }

    @Test
    public void testTagDocumentWithProjectWithRouting() throws Exception {
        when(repository.tag(eq(project("prj1")), any(), eq(tag("tag1")), eq(tag("tag2")))).thenReturn(true).thenReturn(false);
        when(indexer.tag(eq(project("prj1")), any(), eq("routing"), eq(tag("tag1")), eq(tag("tag2")))).thenReturn(true).thenReturn(false);

        put("/api/document/project/tag/prj1/doc_id?routing=routing", "[\"tag1\", \"tag2\"]").should().respond(201);
        put("/api/document/project/tag/prj1/doc_id?routing=routing", "[\"tag1\", \"tag2\"]").should().respond(200);

        verify(indexer, times(2)).tag(eq(project("prj1")), any(), eq("routing"), eq(tag("tag1")), eq(tag("tag2")));
    }


    @Test
    public void testGetStarredDocuments() throws Exception {
        when(repository.getStarredDocuments(any())).thenReturn(asList(createDoc("doc1"), createDoc("doc2")));
        get("/api/document/starred").should().respond(200).haveType("application/json").contain("\"doc1\"").contain("\"doc2\"");
    }

    @Override
    public int port() { return server.port();}

    private Document createDoc(String name) {
        return new Document(project("prj"), "docid", Paths.get("/path/to/").resolve(name), name,
                FRENCH, Charset.defaultCharset(),
                "text/plain", new HashMap<>(), Document.Status.INDEXED,
                new HashSet<>(), new Date(), null, null,
                0, 123L);
    }
}