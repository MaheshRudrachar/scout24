package de.scout24.controller;

import com.google.common.collect.Lists;
import de.scout24.model.WebDocument;
import de.scout24.thread.ResourceValidation;
import de.scout24.util.HtmlParserUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

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
            webDocument.setLinkResourceValidationMap(runResouceValidation(hyperLinksCollection));

        } catch (IOException e) {
            e.printStackTrace();
        }

        model.addAttribute("webDocument", webDocument);
        return "result";
    }

    /**
     * With all the links(resources) on the web document, validate if the resource is available
     *
     * @param resourceList
     * @return
     */
    public Map<String, Integer> runResouceValidation(List<String> resourceList) {

        Map<String, Integer> resourceValidationMap = new HashMap<>();

        //Consider performance here, use ExecutorService as multi-thread pool to check all the resources
        ExecutorService executor = Executors.newFixedThreadPool(10);

        //create a list to hold the Future object associated with Callable
        List<FutureTask<Map<String, Integer>>> futureTaskList = new ArrayList<>();

        //use Guava Lists.partition to partition resource list into subsets
        List<List<String>> subSets = Lists.partition(resourceList, 10);

        for (List<String> subList: subSets) {
            ResourceValidation task = new ResourceValidation(subList);
            FutureTask<Map<String, Integer>> futureTask = new FutureTask<>(task);

            //submit Callable tasks to be executed by thread pool
            executor.submit(futureTask);
            futureTaskList.add(futureTask);
        }

        for (FutureTask<Map<String, Integer>> completeFutureTask: futureTaskList) {
            try {
                resourceValidationMap.putAll(completeFutureTask.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return resourceValidationMap;
    }
}
