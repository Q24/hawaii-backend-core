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

import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LogManager;

import java.io.File;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;

public class FileChangeListener implements FileListener {

    private FileChangeHandler handler;
    private final long maxFileLength;
    private final LogManager logManager;
    private final DefaultFileMonitor fm;
    private String[] fileLocations;

    private boolean started = false;

    public FileChangeListener(long maxFileLength, LogManager logManager) {
        this.maxFileLength = maxFileLength * 1024 * 1024;
        this.logManager = logManager;
        fm = new DefaultFileMonitor(this);
        // setRecursive must be called before the addFile method to detect
        // changes in existing files
        fm.setRecursive(true);
    }

    public void startMonitoringFile(String... fileLocations) throws ServerException {
        try {
            this.fileLocations = fileLocations;
            startListening();
            if (!started) {
                fm.start();
                started = true;
            }
        } catch (Throwable e) {
            throw new ServerException(ServerError.UNEXPECTED_EXCEPTION, "could not initiate filechangelistener for file at " + fileLocations, e);
        }
    }

    public void pauseListening() throws ServerException {
        if (fileLocations == null) {
            return;
        }
        try {
            FileSystemManager fsManager = VFS.getManager();

            for (String fileLocation : fileLocations) {
                File file = new File(fileLocation);
                FileObject fileObject = fsManager.resolveFile(file.getAbsolutePath());
                logManager.info(CoreLoggers.SERVER, "Stop listening to '" + fileLocation + "'.");
                fm.removeFile(fileObject);
            }
        } catch (Throwable e) {
            throw new ServerException(ServerError.UNEXPECTED_EXCEPTION, "could not initiate filechangelistener for file at " + fileLocations, e);
        }
    }

    public void startListening() throws ServerException {
        if (fileLocations == null) {
            return;
        }

        try {
            FileSystemManager fsManager = VFS.getManager();

            for (String fileLocation : fileLocations) {
                File file = new File(fileLocation);
                FileObject fileObject = fsManager.resolveFile(file.getAbsolutePath());
                logManager.info(CoreLoggers.SERVER, "Start listening to '" + fileLocation + "'.");
                fm.addFile(fileObject);
            }
        } catch (Throwable e) {
            throw new ServerException(ServerError.UNEXPECTED_EXCEPTION, "could not initiate filechangelistener for file at " + fileLocations, e);
        }
    }

    public void setCallBack(FileChangeHandler handler) {
        this.handler = handler;
    }

    @Override
    public void fileChanged(FileChangeEvent fce) throws Exception {
        if (fce != null) {
            FileObject file = fce.getFile();
            if (file.getContent().getSize() > maxFileLength) {
                logManager.error(new ServerException(ServerError.PARSER_MAX_FILESIZE_EXCEEDED, "Handler '" + handler + "', file at: "
                        + file.getName().getPath()));
            } else {
                logManager.info(CoreLoggers.SERVER, "File changed '" + fce.getFile().getName().getPath() + "'.");
                handler.handleFileChange();
            }
        }
    }

    @Override
    public void fileCreated(FileChangeEvent arg0) throws Exception {
        fileChanged(arg0);
    }

    @Override
    public void fileDeleted(FileChangeEvent arg0) throws Exception {
        // unused
    }

    public void stop() {
        fm.stop();
    }
}
