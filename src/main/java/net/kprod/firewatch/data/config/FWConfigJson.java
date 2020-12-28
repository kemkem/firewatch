
package net.kprod.firewatch.data.config;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "defaults",
    "recipients",
    "elements"
})
public class FWConfigJson {

    @JsonProperty("defaults")
    private Defaults defaults;
    @JsonProperty("recipients")
    private List<Recipient> recipients = null;
    @JsonProperty("elements")
    private List<Element> elements = null;

    @JsonProperty("defaults")
    public Defaults getDefaults() {
        return defaults;
    }

    @JsonProperty("defaults")
    public void setDefaults(Defaults defaults) {
        this.defaults = defaults;
    }

    @JsonProperty("recipients")
    public List<Recipient> getRecipients() {
        return recipients;
    }

    @JsonProperty("recipients")
    public void setRecipients(List<Recipient> recipients) {
        this.recipients = recipients;
    }

    @JsonProperty("elements")
    public List<Element> getElements() {
        return elements;
    }

    @JsonProperty("elements")
    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

}
