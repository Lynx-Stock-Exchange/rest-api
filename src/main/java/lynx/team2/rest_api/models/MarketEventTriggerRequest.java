package lynx.team2.rest_api.models;

public class MarketEventTriggerRequest {
    private String event_type;
    private String scope;
    private String target;
    private Double magnitude;
    private Integer duration_ticks;
    private String headline;

    public String getEvent_type() { return event_type; }
    public String getScope() { return scope; }
    public String getTarget() { return target; }
    public Double getMagnitude() { return magnitude; }
    public Integer getDuration_ticks() { return duration_ticks; }
    public String getHeadline() { return headline; }

    public void setEvent_type(String event_type) { this.event_type = event_type; }
    public void setScope(String scope) { this.scope = scope; }
    public void setTarget(String target) { this.target = target; }
    public void setMagnitude(Double magnitude) { this.magnitude = magnitude; }
    public void setDuration_ticks(Integer duration_ticks) { this.duration_ticks = duration_ticks; }
    public void setHeadline(String headline) { this.headline = headline; }
}
