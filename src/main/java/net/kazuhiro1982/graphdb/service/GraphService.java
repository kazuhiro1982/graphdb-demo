package net.kazuhiro1982.graphdb.service;

import java.util.List;

import net.kazuhiro1982.graphdb.form.PostForm;
import net.kazuhiro1982.graphdb.form.PurchaseForm;
import net.kazuhiro1982.graphdb.model.Book;
import net.kazuhiro1982.graphdb.model.User;

public interface GraphService {

    public List<User> users();

    public List<Book> books();

    public void createBook(PostForm postForm);

    public void purchaseBook(PurchaseForm purchaseForm);

    public void reset();

}
