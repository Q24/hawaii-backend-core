/**
 * Copyright 2015 Q24
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.kahu.hawaii.service.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.IOFileFilter;

public class FileDirectoryTreeWalker extends DirectoryWalker<String> {
    private String startDirectoryPath;
    /**
     * controls how deep the hierarchy is navigated to (less than 0 means
     * unlimited)
     */
    private static final int TREEWALKER_DEPTH_LIMIT = -1;

    public FileDirectoryTreeWalker(IOFileFilter dirFilter, IOFileFilter fileFilter) {
        super(dirFilter, fileFilter, TREEWALKER_DEPTH_LIMIT);
    }

    public List<String> walk(File startDirectory) throws IOException {
        startDirectoryPath = startDirectory.getPath();
        List<String> results = new ArrayList<String>();
        walk(startDirectory, results);
        return results;
    }
    
    protected void handleFile(File file, int depth, Collection<String> results) {
        // strip the start directory from the filepath.
        String path = file.getPath();
        
        String fileName = path.substring(startDirectoryPath.length(), path.length());
        
        results.add(fileName);
      }
}
