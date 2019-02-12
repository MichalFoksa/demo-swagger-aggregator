package net.michalfoksa.demo.swagger.aggregator.http.web;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.michalfoksa.demo.swagger.aggregator.context.RuntimeContext;

@Controller
@RequestMapping(path="/")
public class ApplicationController {

    @Inject
    private RuntimeContext runtimeContext;

    @GetMapping()
    public @ResponseBody String getApplicationInfo() {
        StringBuilder sb = new StringBuilder();

        sb.append("<table>\n<tr>");
        String runtimeTable = runtimeContext.getAllFieldsMap().entrySet().stream()
                .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
                .map(entry -> "<td>" + entry.getKey() + ":</td> <td>" + entry.getValue().toString() + "</td>")
                .collect(Collectors.joining("</tr>\n<tr>"));
        sb.append(runtimeTable).append("</tr>\n</table>\n");

        return sb.toString();
    }

}
