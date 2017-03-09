package de.scout24.controller;

import de.scout24.model.WebDocument;
import de.scout24.util.HtmlParserUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.List;

@Controller
public class HtmlAnalyzerController {

    @GetMapping("/analyzer")
    public String inputForm(Model model) {
        model.addAttribute("webDocument", new WebDocument());
        return "main";
    }

    @PostMapping("/analyzer")
    public String htmlAnalyzer(@ModelAttribute WebDocument webDocument, Model model) {
        try {
            Document doc = Jsoup.connect(webDocument.getUri()).get();

            List<String> hyperLinksCollection = HtmlParserUtil.getHyperLinksCollection(doc);
            int[] numOfLinks = HtmlParserUtil.getNumOfHyperMediaLink(doc, hyperLinksCollection);

            webDocument.setHtmlVersion(HtmlParserUtil.getHtmlDocType(doc));
            webDocument.setTitle(HtmlParserUtil.getTitle(doc));
            webDocument.setHeadingTagMap(HtmlParserUtil.getNumOfHeadingsByGroup(doc));
            webDocument.setNumOfInternalLinks(numOfLinks[0]);
            webDocument.setNumOfExternalLinks(numOfLinks[1]);
            webDocument.setHasLoginForm(HtmlParserUtil.hasLogin(doc));

        } catch (IOException e) {
            e.printStackTrace();
        }

        model.addAttribute("webDocument", webDocument);
        return "result";
    }
}
