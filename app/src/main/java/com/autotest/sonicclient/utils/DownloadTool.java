/*
 *   sonic-agent  Agent of Sonic Cloud Real Machine Platform.
 *   Copyright (C) 2022 SonicCloudOrg
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.autotest.sonicclient.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

public class DownloadTool {
    public static File download(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        File file = null;
        try(InputStream is = con.getInputStream()) {
            byte[] bs = new byte[1024];
            int len;

            long time = Calendar.getInstance().getTimeInMillis();
            String tail = "";
            if (urlString.lastIndexOf(".") != -1) {
                tail = urlString.substring(urlString.lastIndexOf(".") + 1);
            }
            String filename = "test-output" + File.separator + "download-" + time + "." + tail;
            file = new File(filename);
            try (FileOutputStream os = new FileOutputStream(file, true)) {
                while ((len = is.read(bs)) != -1) {
                    os.write(bs, 0, len);
                }
            }
        }

        return file;
    }
}
