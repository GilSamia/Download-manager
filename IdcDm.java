

import java.io.File;
import java.net.MalformedURLException;
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
        List<URL> urlList = new ArrayList<>();
        if (url.startsWith("https://") || url.startsWith("http://")) {
            try {
                urlList.add(new URL(url));
            } catch (MalformedURLException e) {
                System.err.println("OOPS! Something went wrong with creating URL from the address given.\n" + e);
            }
        } else {
            urlList = createUrlList(url);
        }

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

    /**
     * creates url list from the given file using scanner.
     * @param i_list string of the list from the file
     * @return list of URLs.
     */
    private static List<URL> createUrlList(String i_list) {
        List<URL> urlList = new ArrayList<>();
        try{
            Scanner scanner = new Scanner(new File(i_list));
            scanner.useDelimiter(System.lineSeparator() + "|\n");
            while (scanner.hasNext()) {
                String url = scanner.next();
                System.out.println("url: " + url);
                urlList.add(new URL(url));
            }
        } catch (Exception e) {
            System.err.println("OOPS! Something went wrong with creating URL list.\n" + e);
            System.exit(1);
        }

        return urlList;
    }
}
