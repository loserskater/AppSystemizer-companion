package com.loserskater.appsystemizer.objects;


import java.util.Comparator;

public class Package {

    private String label;
    private String packageName;
    private boolean enabled;

    public Package(String packageName, String label, int enabled) {
        if (label != null){
            this.label = label.replace(" ", "").trim();
        }
        this.packageName = packageName;
        this.enabled = enabled == 1;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static class PackageNameComparator implements Comparator<Package> {
        public int compare(Package left, Package right) {
            return left.label.compareToIgnoreCase(right.label);
        }
    }
}
