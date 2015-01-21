// This software is Copyright(c) 2010 Association of Universities for
// Research in Astronomy, Inc.  This software was prepared by the
// Association of Universities for Research in Astronomy, Inc. (AURA)
// acting as operator of the Gemini Observatory under a cooperative
// agreement with the National Science Foundation. This software may 
// only be used or copied as described in the license set out in the 
// file LICENSE.TXT included with the distribution package.

/* Generated by Together */

package edu.gemini.itc.shared;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.InvalidContentTypeException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Class that Allows the Parsing of Multipart posts.
 * Allows text files to be submitted.
 *
 * @author Brian Walls
 * @version 1.0
 */
public class ITCMultiPartParser {
    /**
     * Constructor for the MultipartParser.
     *
     * @param req                The Submitted request.  Must be Multipart.
     * @param MAX_CONTENT_LENGTH Maximum Content length of the submitted request.
     *                           Usefull for limiting the size of the uploaded file.
     * @throws IncompatibleFileTypeException Throws IncompatibleFileTypeException if the submitted file is not
     *                                       of a type that can be handled.
     * @throws ParameterParseException       Thrown if any of the parameters cannot be read.
     * @throws IOException                   Thrown if there is any problem with the connection to the remote computer.
     */
    public ITCMultiPartParser(javax.servlet.http.HttpServletRequest req, int MAX_CONTENT_LENGTH) throws IncompatibleFileTypeException, ParameterParseException, java.io.IOException {
        try {
            //multipartParser = new MultipartParser(req, MAX_CONTENT_LENGTH);
            upload = new DiskFileUpload();

            upload.setSizeMax(MAX_CONTENT_LENGTH);

            items = upload.parseRequest(req);
        } catch (java.lang.NullPointerException e) {//normal end of parameter parsing
        } catch (InvalidContentTypeException e) {
            throw new java.io.IOException("Submitted form is not Multipart.  Contact helpdesk");
        } catch (SizeLimitExceededException e) {
            throw new java.io.IOException("Request Size limit exceeded.  Please submit smaller file.");
        } catch (FileUploadException e) {
            throw new java.io.IOException("Unable to parse multipart request.  Contact helpdesk");

        }

        Iterator iter = items.iterator();

        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();
            try {
                if (item.isFormField()) {
                    parameterNames.add(item.getFieldName());
                    parameters.put(item.getFieldName(), item.getString());
                } else {
                    addFile(item);
                }
            } catch (IncompatibleFileTypeException e) {
                throw new java.io.IOException(e.toString());
            }
        }

    }


    private void addFile(FileItem file) throws IncompatibleFileTypeException {
        if (file.getName().length() == 0) return;  //If there is no filename exit

        if (file.getContentType().equals(TEXT) || file.getName().endsWith(".dat") || file.getName().endsWith(".nm")) {

            files.put(file.getFieldName(), file.getString());
            fileNames.add(file.getName());
            remoteFileNames.put(file.getFieldName(), file.getName());
            fileTypes.put(file.getFieldName(), TEXT);

        } else {

            throw new IncompatibleFileTypeException("Submitted file, " + file.getName()
                    + ", is a " + file.getContentType() + " file which is not supported. ");
        }

    }

    /**
     * Returns the string value of a submitted parameter.
     *
     * @param name String Name of the requested parameter, from the HTML form.
     * @return java.lang.String of the value from the requested Parameter.
     * @throws NoSuchParameterException Thrown if the parameter was not parsed from the HTTP request
     */
    public String getParameter(java.lang.String name) throws NoSuchParameterException {
        String parameter = (String) parameters.get(name);
        if (parameter != null)
            return parameter;
        else
            throw new NoSuchParameterException("Parameter, " + name + " was not found. "
                    + "Contact Helpdesk. ", name);
    }

    /**
     * Returns true if the parameter name has been parsed.
     *
     * @param name String Name of the requested parameter, from the HTML form.
     * @return Boolean. True if parameter exists.
     */
    public boolean parameterExists(java.lang.String name) {
        String parameter = (String) parameters.get(name);
        if (parameter != null)
            return true;
        else
            return false;
    }

    /**
     * Allows access to all of the parameter names in the form of an Iterator.
     *
     * @return Returns an Iterator of all of the Parameter Names.
     */
    public Iterator getParameterNames() {
        return parameterNames.iterator();
    }

    /**
     * Allows access to all of the file names in the form of an Iterator.
     *
     * @return Returns and Iterator of all the file names.
     */
    public Iterator getFileNames() {
        return fileNames.iterator();
    }

    /**
     * Method that allows access to the Remote path and name of the uploaded file
     *
     * @param fileName Local Representation of the remote filename
     * @return Returns the path and filename of the uploaded file.
     */
    public String getRemoteFileName(java.lang.String fileName) {
        return (String) remoteFileNames.get(fileName);
    }

    /**
     * The getFileName method allows access to the file names as an Array.
     * If you know the index of the file you want then you can get its identifier.
     *
     * @param index index of the filename
     * @return returns the file name identifier as a String
     */
    public String getFileName(int index) {
        return (String) fileNames.get(index);
    }

    /**
     * The getTextFile method allows access to any file uploaded of type <CODE>text/plain</CODE>
     *
     * @param fileName the fileName identifier of the text file passed (from the html form)
     * @return returns the text file as a String.
     * @throws IncompatibleFileTypeException If requested file is not of type <CODE>text/plain</CODE> an IncompatableFileTypeException is thrown.
     */
    public String getTextFile(java.lang.String fileName) throws IncompatibleFileTypeException {
        if (((String) fileTypes.get(fileName)).equals(TEXT))
            return (String) files.get(fileName);
        else
            throw new IncompatibleFileTypeException("Submitted file is not a " +
                    "text/plain file.  Resubmit with a Text file");
    }

    /**
     * Not supported yet.
     *
     * @param fileName String fileName identifier for the binary file.
     * @return returns null now
     */
    public byte[] getBinaryFile(java.lang.String fileName) {
        byte[] b1 = null;
        return b1;
    }

    /**
     * This method allows access to the file types of the files
     * uploaded.
     *
     * @param fileName fileMame identifier from the HTML form.
     * @return Returns a string of the MIME type ie <CODE>text/plain</CODE>
     */
    public String getFileType(java.lang.String fileName) {
        return (String) fileTypes.get(fileName);
    }


    private static String TEXT = "text/plain";
    //private MultipartParser multipartParser;
    private DiskFileUpload upload;
    private List /* FileItem */ items;
    private ArrayList fileNames = new ArrayList();
    private ArrayList parameterNames = new ArrayList();
    private HashMap files = new HashMap();
    private HashMap parameters = new HashMap();
    private HashMap fileTypes = new HashMap();
    private HashMap remoteFileNames = new HashMap();
}
