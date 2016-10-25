package com.loserskater.systemizer.objects;


import java.util.Comparator;

public class Package {

    private String label;
    private String packageName;

    public Package(String label, String packageName) {
        this.label = label;
        this.packageName = packageName;
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

    public static class PackageNameComparator implements Comparator<Package> {
        public int compare(Package left, Package right) {
            return left.label.compareToIgnoreCase(right.label);
        }
    }
}
