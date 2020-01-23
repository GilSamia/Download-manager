package LabDm.src;

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

        if (args.length == 2) {
            numOfThreads = Integer.parseInt(args[1]);
        }
        System.out.println("Downloading...");

        if (numOfThreads > 1) {
            System.out.println(" using " + numOfThreads + " connections...");
        }
        HttpRangeGetter.justACheck();
        DownloadManager downloadManager = new DownloadManager(url, numOfThreads); //TODO: send to manager url list
        downloadManager.startDownload();
    }
}
