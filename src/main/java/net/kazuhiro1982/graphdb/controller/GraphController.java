package net.kazuhiro1982.graphdb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.kazuhiro1982.graphdb.form.PostForm;
import net.kazuhiro1982.graphdb.form.PurchaseForm;
import net.kazuhiro1982.graphdb.service.GraphService;

@Controller
public class GraphController {

    @Autowired
    private GraphService graphService;

    @RequestMapping("/")
    public String list(Model model) {
        model.addAttribute("books", graphService.books());
        model.addAttribute("users", graphService.users());
        return "index";
    }

    @RequestMapping(value = "/book", method = RequestMethod.POST)
    public String postBook(@ModelAttribute PostForm post) {
        graphService.createBook(post);
        return "redirect:/";
    }

    @RequestMapping(value = "/purchase", method = RequestMethod.POST)
    public String purchase(@ModelAttribute PurchaseForm purchase) {
        graphService.purchaseBook(purchase);
        return "redirect:/";
    }

    // for debug endpoint
    // data all clear
    // @RequestMapping(value = "/reset")
    public String reset() {
        graphService.reset();
        return "redirect:/";
    }

}
