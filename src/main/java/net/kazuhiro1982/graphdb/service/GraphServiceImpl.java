package net.kazuhiro1982.graphdb.service;

import static org.apache.tinkerpop.gremlin.process.traversal.P.neq;
import static org.apache.tinkerpop.gremlin.process.traversal.P.without;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.kazuhiro1982.graphdb.form.PostForm;
import net.kazuhiro1982.graphdb.form.PurchaseForm;
import net.kazuhiro1982.graphdb.model.Book;
import net.kazuhiro1982.graphdb.model.Keyword;
import net.kazuhiro1982.graphdb.model.User;

@Service
public class GraphServiceImpl implements GraphService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GraphTraversalSource g;

    public List<User> users() {
        GraphTraversal<Vertex, Map<String, Object>> t = g.V().limit(1000).hasLabel("User").has("name").valueMap();
        return t.toStream().map(v -> {
            User user = new User();
            user.setName(getValue(v.get("name")));
            user.setRecommends(recommendsTitle(user));
            user.setPurchased(purchased(user));
            return user;
        }).collect(Collectors.toList());
    }

    public List<Book> books() {
        GraphTraversal<Vertex, Map<String, Object>> t = g.V().limit(1000).hasLabel("Page").has("title").valueMap();

        return t.toStream().map(v -> {
            Book page = new Book();
            String pageTitle = getValue(v.get("title"));
            page.setTitle(pageTitle);
            GraphTraversal<Vertex, Map<String, Object>> t2 = g.V().hasLabel("Page").has("title", pageTitle).out("tagged").valueMap();

            String tags = t2.toStream().map(m -> getValue(m.get("phrase"))).collect(Collectors.joining(","));
            page.setKeywords(tags);

            GraphTraversal<Vertex, Map<String, Object>> t3 = g.V().hasLabel("Page").has("title", pageTitle).in("created").valueMap();
            t3.toStream().map(m -> getValue(m.get("name"))).findFirst().ifPresent(author -> page.setAuthor(author));

            return page;
        }).collect(Collectors.toList());
    }

    public void createBook(PostForm postForm) {
        logger.info("post page:[author:" + postForm.getAuthor() + ", title:" + postForm.getTitle() + "]");
        User user = new User();
        user.setName(postForm.getAuthor());
        Book page = new Book();
        page.setTitle(postForm.getTitle());
        addUser(user);
        addBook(user, page);
        if (postForm.getKeywords() != null && postForm.getKeywords().length() > 0) {
            String[] tags = postForm.getKeywords().split(",");
            for (String tag : tags) {
                Keyword keyword = new Keyword();
                keyword.setPhrase(tag);
                addKeyword(page, keyword);
            }
        }
    }

    public void purchaseBook(PurchaseForm purchaseForm) {
        logger.info("purchase page:[consumer:" + purchaseForm.getUserName() + ", title:" + purchaseForm.getBookTitle() + "]");
        User user = new User();
        user.setName(purchaseForm.getUserName());
        Book page = new Book();
        page.setTitle(purchaseForm.getBookTitle());
        addUser(user);
        purchase(user, page);
    }

    public void reset() {
        logger.info("drop");
        g.E().limit(1000).drop().iterate();
        g.V().limit(1000).drop().iterate();
    }

    private void addBook(User author, Book page) {
        if (!g.V().hasLabel("Page").has("title", page.getTitle()).hasNext()) {
            g.addV("Page").property("title", page.getTitle()).iterate();
        }
        g.V().hasLabel("Page").has("title", page.getTitle()).as("page")
                .V().hasLabel("User").has("name", author.getName()).addE("created").to("page").iterate();
    }

    private void addKeyword(Book page, Keyword keyword) {
        logger.info("add keyword:[page:" + page.getTitle() + ", keyword:" + keyword.getPhrase() + "]");
        if (!g.V().hasLabel("Keyword").has("phrase", keyword.getPhrase()).hasNext()) {
            g.addV("Keyword").property("phrase", keyword.getPhrase()).iterate();
        }
        g.V().hasLabel("Keyword").has("phrase", keyword.getPhrase()).as("keyword")
                .V().hasLabel("Page").has("title", page.getTitle()).addE("tagged").to("keyword").iterate();
    }

    private void purchase(User consumer, Book page) {
        g.V().hasLabel("Page").has("title", page.getTitle()).as("page")
                .V().hasLabel("User").has("name", consumer.getName()).addE("purchase").to("page").iterate();
    }

    private List<String> recommendsTitle(User user) {
        GraphTraversal<Vertex, Map<String, Object>> t = g.V().hasLabel("User").has("name", user.getName()).as("user")
                .out("purchase").aggregate("purchased")
                .in("purchase").where(neq("user"))
                .out("purchase").where(without("purchased")).valueMap();
        return t.toStream().map(v -> getValue(v.get("title"))).collect(Collectors.toList());
    }

    private List<String> purchased(User user) {
        logger.info("aggregate purchased:" + user.getName());
        GraphTraversal<Vertex, Map<String, Object>> t = g.V().hasLabel("User").has("name", user.getName()).out("purchase").valueMap();
        return t.toStream().map(v -> getValue(v.get("title"))).collect(Collectors.toList());
    }

    private void addUser(User user) {
        if (!g.V().hasLabel("User").has("name", user.getName()).hasNext()) {
            g.addV("User").property("name", user.getName()).iterate();
        }
    }

    private String getValue(Object obj) {
        String v = obj.toString();
        if (v.startsWith("[")) {
            v = v.substring(1);
        }
        if (v.endsWith("]")) {
            v = v.substring(0, v.length() - 1);
        }
        return v;
    }

}
