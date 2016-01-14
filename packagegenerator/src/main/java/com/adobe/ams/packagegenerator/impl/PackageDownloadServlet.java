package com.adobe.ams.packagegenerator.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.jackrabbit.vault.fs.api.PathFilterSet;
import org.apache.jackrabbit.vault.fs.api.WorkspaceFilter;
import org.apache.jackrabbit.vault.fs.config.DefaultWorkspaceFilter;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.apache.jackrabbit.vault.util.JcrConstants;
import org.apache.jackrabbit.vault.util.Text;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;

@SuppressWarnings("serial")
@SlingServlet(
        resourceTypes = "/apps/packagereplication/treeactivation",
        selectors = "viapackage",
        extensions = "json",
        methods = "GET",
        generateComponent = true)
public class PackageDownloadServlet extends SlingSafeMethodsServlet {

    private static final Logger log = LoggerFactory
            .getLogger(PackageDownloadServlet.class);

    private static final String DIR="/tmp/";
    @Reference
    private Packaging packaging;

    @Override
    public final void doGet(SlingHttpServletRequest slingHttpServletRequest,
                             SlingHttpServletResponse slingHttpServletResponse) throws
            ServletException, IOException {
        String path = cleanPath(URLDecoder.decode(slingHttpServletRequest.getParameter
                ("path"),"UTF-8"));
        log.debug("Requesting for package generation of rootpath {0}",path);
        if (path.lastIndexOf('/') <= 0) {
            if (path.length() == 0) {
                path = "/";
            }
            slingHttpServletResponse.getWriter().printf("<div " +
                    "class=\"error\">" + "Cowardly refusing to tree-activate " +
                    "'"+path+"'" + "</div>");
            return;
        }
        try {
            String fileSuffix =path.substring(path.lastIndexOf("/")+1)
                    +".zip";
            File file = new File(DIR+fileSuffix);
           // FileOutputStream fileOutputStream=FileUtils.openOutputStream
            // (file);

          //  fileOutputStream.close();
         //   FileInputStream fileInputStream = FileUtils.openInputStream(file);
            slingHttpServletResponse.setContentType("application/octet-stream");
            String ua = slingHttpServletRequest.getHeader("User-Agent");

            if (ua.contains("MSIE 9.0") || ua.contains("MSIE 10.0") || ua.contains("Firefox")
                    || ua.contains("Chrome") || ua.contains("Opera"))
                slingHttpServletResponse.addHeader("Content-Disposition",
                        "attachment; filename*=UTF-8''" + Text.escape(fileSuffix));
            else
                slingHttpServletResponse.addHeader("Content-Disposition",
                        "filename=\"" + Text.escape(fileSuffix) + "\"");

//            slingHttpServletResponse.addHeader("Content-Length", String
//                    .valueOf(fileInputStream.getChannel().size()));
            buildPackage(slingHttpServletRequest.getResourceResolver(),path,
                    fileSuffix,slingHttpServletResponse.getOutputStream());
            try{
//                IOUtils.copy(fileInputStream, slingHttpServletResponse
//                        .getOutputStream());
            }finally {
         //    IOUtils.closeQuietly(fileInputStream);
            }


        } catch (Exception e) {
            log.error("Package could not be generated for "+path,e);
        }
    }
    private void buildPackage(ResourceResolver resourceResolver, String
            rootPath, String fileSuffix, ServletOutputStream fileOutputStream) throws
            IOException,
            RepositoryException,
            PackageException {
        JcrPackageManager packageManager = getPackageManager(resourceResolver);


        JcrPackage jcrPackage=packageManager.create("activatetree",
                fileSuffix
        );
        jcrPackage.getDefinition().setFilter(buildWorkspaceFilter
                (resourceResolver,rootPath),true);

        packageManager.assemble(jcrPackage.getDefinition(),null,fileOutputStream);

    }
    private String cleanPath(String path){
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
    private WorkspaceFilter buildWorkspaceFilter(ResourceResolver
                                                         resourceResolver, String
                                                         rootPath){
        DefaultWorkspaceFilter defaultWorkspaceFilter = new
                DefaultWorkspaceFilter();
        defaultWorkspaceFilter.add(new PathFilterSet(rootPath));
        return defaultWorkspaceFilter;
    }
    private JcrPackageManager getPackageManager(ResourceResolver resourceResolver){
        Session session = resourceResolver.adaptTo(Session
                .class);
        JcrPackageManager packageManager = packaging.getPackageManager(session);
        return packageManager;
    }
}
