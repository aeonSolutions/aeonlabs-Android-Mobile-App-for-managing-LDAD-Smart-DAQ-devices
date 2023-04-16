package aeonlabs.iot.data.acquisition.enterprise.model;

import java.io.Serializable;

public class SettingsDB implements Serializable {
    private String settingsId ;
    private String cloudAPIaddress ;

    public String getSettingsId() {
        return settingsId;
    }

    public void setSettingsId(String _settingsId) {
        this.settingsId = _settingsId;
    }

    public String getCloudAPIaddress() {
        return cloudAPIaddress;
    }

    public void setCloudAPIaddress(String _cloudAPIaddress) {
        this.cloudAPIaddress = _cloudAPIaddress;
    }

    public SettingsDB(String cloudAPIaddress) {
        this.cloudAPIaddress = cloudAPIaddress;
    }

    public SettingsDB(){
    }
}