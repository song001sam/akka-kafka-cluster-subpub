package sample.cluster.simple;

public class SimpleClusterApp {

    public static void main(String[] args) {
        if (args.length == 0) Launcher.startup(new String[]{"2551", "2552"});
        else Launcher.startup(args);
    }


}
