package com.adobe.ams.kalyanar.samples.impl;

import com.day.cq.dam.api.Asset;
import com.day.cq.replication.ReplicationStatus;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.jcr.vault.fs.api.PathFilterSet;
import com.day.jcr.vault.fs.api.WorkspaceFilter;
import com.day.jcr.vault.fs.config.DefaultWorkspaceFilter;
import com.day.jcr.vault.packaging.JcrPackage;
import com.day.jcr.vault.packaging.JcrPackageManager;
import com.day.jcr.vault.packaging.PackageException;
import com.day.jcr.vault.packaging.Packaging;
import com.day.jcr.vault.util.Text;
import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Properties;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by kalyanar on 8/10/2015.
 */
@Component()
@Service
@Properties({
        @Property(name="felix.webconsole.label", value="build modified pages and assets package"),
        @Property(name="felix.webconsole.title", value="build modified pages and assets package"),
        @Property(name="felix.webconsole.category", value="Sling")
})
public class WebConsolePlugin extends HttpServlet{

    String pkgGroupName = "AMSPackageDemo";
    String packageName = "modifiedResourcePackage";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private Packaging packaging;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse res)
            throws ServletException, IOException {
        final PrintWriter pw = res.getWriter();
        writeFormToScreen(pw);
        if(isErrorBuildingPackage(req.getParameter("error"))){
            writeErrorMessageToScreen(pw);
        }else if(isSuccessBuildingPackage(req.getParameter("packagePath"))){
            writePackageDownloadFormToScreen(pw,req.getParameter("packagePath"));
        }
    }
    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse res)
            throws ServletException, IOException {

        boolean error = false;
        String packagePath = getPackagePath();
        try {
            ResourceResolver serviceResourceResolver = getServiceResourceResolver();
            String rootPagesPath = getRootPath(req.getParameter("rootPagesPath"));
            String rootAssetsPath = getRootPath(req.getParameter("rootAssetsPath"));
            // reject root and 1 level paths
            if ( isRootActualContentRoot(rootPagesPath) ||  isRootActualContentRoot(rootAssetsPath) ) {
                writeWontGeneratePackageMessageToScreen(res.getWriter());
                return;
            }
            buildPackage(serviceResourceResolver,rootPagesPath,rootAssetsPath,packagePath);
        } catch (LoginException e) {
            error = true;
        } catch (RepositoryException e) {
            error = true;
        } catch (PackageException e) {
            error = true;
        }
        String redirectPath = getRedirectPath(req.getPathInfo());
        if(error){
            res.sendRedirect(redirectPath + "?error=error");
        }else{
            res.sendRedirect(redirectPath + "?packagePath=" + packagePath);
        }
    }

    private boolean isSuccessBuildingPackage(String packagePath){
        if (packagePath!=null && !"".equals(packagePath)){
            return true;
        }
        return false;
    }
    private boolean isErrorBuildingPackage(String error){
        if("error".equals(error)){
            return true;
        }
        return false;
    }
    private String getRedirectPath(String pathInfo){
        String redirectPath=pathInfo;
        if(!redirectPath.startsWith("/system/console")){
            redirectPath="/system/console"+redirectPath;
        }
        return redirectPath;
    }
    private void buildPackage(ResourceResolver serviceResourceResolver, String rootPagesPath, String rootAssetsPath,String packagePath) throws IOException, RepositoryException, PackageException {
        JcrPackageManager jcrPackageManager = getJcrPackageManager(serviceResourceResolver);
        JcrPackage jcrPackage=jcrPackageManager.create(pkgGroupName, packagePath);
        jcrPackage.getDefinition().setFilter(buildWorkSpaceFilter(serviceResourceResolver, rootPagesPath, rootAssetsPath),true);
        jcrPackageManager.assemble(jcrPackage, null);
    }
    private String getPackagePath(){
        return packageName+new Date().getTime()+".zip";
    }
    private WorkspaceFilter buildWorkSpaceFilter(ResourceResolver serviceResourceResolver,String pagesRootPath, String assetsRootPath){
        List<String> pathsToAddToFilter = getPathsToAddToFilter(serviceResourceResolver,pagesRootPath,assetsRootPath);
        DefaultWorkspaceFilter defaultWorkspaceFilter = new DefaultWorkspaceFilter();
        for(String path : pathsToAddToFilter){
            defaultWorkspaceFilter.add(new PathFilterSet(path));
        }
        return defaultWorkspaceFilter;
    }
    private List<String> getPathsToAddToFilter(ResourceResolver serviceResourceResolver,String pagesRootPath, String assetsRootPath){
        List<String> validPagesPathList = buildValidPagesPathList(serviceResourceResolver, pagesRootPath);
        List<String> validAssetsPathList = buildValidAssetsPathList(serviceResourceResolver,assetsRootPath);
        List<String> pathsToAddToFilter = new ArrayList<String>(validPagesPathList);
        pathsToAddToFilter.addAll(validAssetsPathList);
        return pathsToAddToFilter;
    }
    private List<String> buildValidAssetsPathList(ResourceResolver serviceResourceResolver, String assetsRootPath){
        List<String> modifiedAfterLastActivationAssetsList = new ArrayList<String>();
        for(Iterator<Resource> assetsIterator = findResources(serviceResourceResolver,assetsRootPath,"dam:AssetContent");assetsIterator.hasNext();){
            Resource assetResource = assetsIterator.next();
            if(isAssetModiedAfterLastActivation(assetResource)){
                modifiedAfterLastActivationAssetsList.add(assetResource.getPath());
            }
        }
        return modifiedAfterLastActivationAssetsList;
    }
    private List<String> buildValidPagesPathList(ResourceResolver serviceResourceResolver, String pagesRootPath){
        List<String> modifiedAfterLastActivationPagesList = new ArrayList<String>();
        PageManager pageManager = serviceResourceResolver.adaptTo(PageManager.class);
        for(Iterator<Resource> pagesIterator =  findResources(serviceResourceResolver,pagesRootPath,"cq:PageContent");pagesIterator.hasNext();){
            Resource pageContentResource = pagesIterator.next();
            if(isPageModifiedAfterLastActivation(pageManager, pageContentResource)){
                modifiedAfterLastActivationPagesList.add(pageContentResource.getPath());
            }
        }
        return modifiedAfterLastActivationPagesList;
    }
    private boolean isAssetModiedAfterLastActivation(Resource assetResource){
        ReplicationStatus replicationStatus = getReplicationStatusOfResource(assetResource);
        Asset asset = assetResource.getParent().adaptTo(Asset.class);
        if(isLastModifiedTimeGreaterThanLastActivationTime(getLastModifiedTimeofAssetInMillis(asset),getLastActivatedTimeInMillis(replicationStatus))){
            return true;
        }
        return false;
    }
    private boolean isPageModifiedAfterLastActivation(PageManager pageManager,Resource pageContentResource){
        ReplicationStatus replicationStatus = getReplicationStatusOfResource(pageContentResource);
        Page page = pageManager.getContainingPage(pageContentResource);
        if(isLastModifiedTimeGreaterThanLastActivationTime(getLastModifiedTimeofPageInMillis(page),getLastActivatedTimeInMillis(replicationStatus))){
            return true;
        }
        return false;
    }
    private boolean isLastModifiedTimeGreaterThanLastActivationTime(long lastModifiedTime,long lastActivationTime){
        if(lastModifiedTime > lastActivationTime){
            return true;
        }
        return false;
    }
    private long getLastActivatedTimeInMillis(ReplicationStatus replicationStatus){
        if(isPageAlreadyPublished(replicationStatus)){
            return replicationStatus.getLastPublished().getTimeInMillis();
        }
        return 0L;
    }
    private long getLastModifiedTimeofAssetInMillis(Asset asset){
        if(asset!=null){
            return asset.getLastModified();
        }
        return 0L;
    }
    private long getLastModifiedTimeofPageInMillis(Page page){
        if(page!=null){
            return page.getLastModified().getTimeInMillis();
        }
        return 0L;
    }
    private boolean isPageAlreadyPublished(ReplicationStatus replicationStatus){
        if(replicationStatus!=null&&replicationStatus.getLastPublished()!=null){
            return true;
        }
        return false;
    }
    private ReplicationStatus getReplicationStatusOfResource(Resource resource){
        return resource.adaptTo(ReplicationStatus.class);
    }
    private Iterator<Resource> findResources(ResourceResolver serviceResourceResolver, String rootPath,String contentType ){
        return serviceResourceResolver.findResources(rootPath + "//element(*," + contentType + ")", "xpath");
    }
    private JcrPackageManager getJcrPackageManager(ResourceResolver serviceResourceResolver){
        Session session=serviceResourceResolver.adaptTo(Session.class);
        JcrPackageManager  jcrPackageManager = packaging.getPackageManager(session);
        return jcrPackageManager;
    }
    private boolean isRootActualContentRoot(String root){
        if("/".equals(root)||"/jcr:root/".equals(root)||"/jcr:root".equals(root)){
            return true;
        }
        return false;
    }
    private String getRootPath(String path){
        String root = path;
        while (root.endsWith("/")) {
            root = root.substring(0, root.length() - 1);
        }
        if (root.length() == 0) {
            root = "/";
        }
        if(!root.startsWith("/jcr:root")){
            root="/jcr:root"+root;
        }
        return root;
    }
    private ResourceResolver getServiceResourceResolver() throws LoginException {
//        Map<String, Object> subserviceParam = new HashMap<String, Object>();
//        subserviceParam.put(ResourceResolverFactory.SUBSERVICE, "readContentService");
//        ResourceResolver serviceResolver = resourceResolverFactory.getServiceResourceResolver(subserviceParam);
        ResourceResolver serviceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
        return serviceResolver;
    }
    private void writeWontGeneratePackageMessageToScreen(PrintWriter pw) throws IOException {
        pw.println("<br/><div class=\"error\">Cowardly refusing to tree-activate \"%s\"</div>");
    }
    private void writeErrorMessageToScreen(PrintWriter pw){
        pw.println("<br/><h1>Error building package</h1>");
    }
    private void writePackageDownloadFormToScreen(PrintWriter pw,String packagePath){
        String docroot = "/crx/packmgr/download.jsp";
        pw.println("<form method=\"get\" action=\""+docroot+"\">");
        pw.println("<input type=\"hidden\" name=\"path\" id=\"path\" value=\"/etc/packages/"+ Text.escape(pkgGroupName)+"/"+Text.escape(packagePath)+"\"></input><br/>");
        pw.println("<input type=\"submit\" value=\"Download recently generated Package\" />");
        pw.println("</form><br/>");
    }
    private void writeFormToScreen(PrintWriter pw){
        pw.println("<form name=\"newpackage\" method=\"post\">");
        pw.println("Enter the Base Path for pages...( example (/content/<yourpath>) (If no path given / is selected..ex., /content/az): )");
        pw.println("<input type=\"text\" name=\"rootPagesPath\" id=\"root\" style=\"width:200px\"><br/>");
        pw.println("Enter the Base Path for assets...( example (/content/dam/<yourpath>) (If no path given / is selected..ex., /content/dam/az): )");
        pw.println("<input type=\"text\" name=\"rootAssetsPath\" id=\"root\" style=\"width:200px\"><br/>");
        pw.println("<input type=\"submit\" value=\"generate package\" />");
        pw.println("</form><br/>");
    }
}
