package cwa115.trongame.Lists;

public class StatsListItem {
    private String title, propertyName, propertyValue;

    // Constructor for the LobbyListItem class
    public StatsListItem(String title) {
        super();
        this.title = title;
        this.propertyName = null;
        this.propertyValue = null;
    }
    public StatsListItem(String propertyName, String propertyValue){
        super();
        this.title = null;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    public boolean isHeader(){
        return title!=null;
    }
    public String getTitle() {
        return title;
    }
    public String getPropertyName() {
        return propertyName;
    }
    public String getPropertyValue(){
        return propertyValue;
    }

}
