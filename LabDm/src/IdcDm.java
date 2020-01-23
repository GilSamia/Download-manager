package LabDm.src;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class IdcDm {
    /**
     * begin the download after accepting the user's args.
     * @param args
     */
    public static void main(String[] args) {
        int numOfThreads = 1;

        if (args.length < 1 || args.length > 2) {
            System.err.println("OOPS! Something went wrong.\nThe number of arguments is invalid.");
            System.exit(1);
        }
        String url = args[0];
        List<URL> urlList = createUrlList(url);

        if (args.length == 2) {
            numOfThreads = Integer.parseInt(args[1]);
        }
        System.out.println("Downloading...");

        if (numOfThreads > 1) {
            System.out.println(" using " + numOfThreads + " connections...");
        }
        DownloadManager downloadManager = new DownloadManager(urlList, numOfThreads);
        downloadManager.startDownload();
    }




    private static List<URL> createUrlList(String i_list) {
        List<URL> UrlList = new ArrayList<>();
        URL curUrl;
        try{
            Scanner scanner = new Scanner(new File(i_list));
            scanner.useDelimiter(System.lineSeparator());
            while (scanner.hasNext()) {
                curUrl = new URL(scanner.next());
                System.out.println(curUrl);
                UrlList.add(curUrl);
            }
        } catch (Exception e) {
            System.err.println("ooops");
            System.exit(1);
        }

        return UrlList;
    }


}
