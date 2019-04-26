package org.jevis.jeconfig.plugin.Dashboard.widget;

public class HTMLPie {

    public HTMLPie() {
    }

    public String getPiePage() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(
                "<!doctype html>\n" +
                        "<html lang=\"en\">\n" +
                        "    <head>\n" +
                        "        <meta charset=\"utf-8\">\n" +
                        "        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                        "        <title>Chartist</title>\n" +
                        "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                        "\n" +
                        "        \n" +
                        "        <link rel=\"stylesheet\" type=\"text/css\" href=\"https://rawgit.com/gionkunz/chartist-js/master/dist/chartist.min.css\">\n" +
                        "        \n" +
                        "        <style>\n" +
                        "            #chart {\n" +
                        "                height: 100%;\n" +
                        "                width: 100%;\n" +
                        "            }\n" +
                        "        </style>\n" +
                        "        \n" +
                        "    </head>\n" +
                        "    <body>\n" +
                        "        \n" +
                        "        <div class=\"ct-chart\" id=\"chart\"></div>\n" +
                        "\n" +
                        "        <script type=\"text/javascript\" src=\"https://rawgit.com/gionkunz/chartist-js/master/dist/chartist.js\"></script>\n" +
                        "        \n" +
                        "        <script>\n" +
                        "        \n" +
                        "        new Chartist.Line('#chart', {\n" +
                        "          labels: [1, 2, 3, 4, 5, 6, 7, 8],\n" +
                        "          series: [\n" +
                        "            [5, 9, 7, 8, 5, 3, 5, 4]\n" +
                        "          ]\n" +
                        "        }, {\n" +
                        "          low: 0,\n" +
                        "          showArea: true\n" +
                        "        });\n" +
                        "        </script>\n" +
                        "    </body>\n" +
                        "</html>");

        return stringBuilder.toString();

    }
}
