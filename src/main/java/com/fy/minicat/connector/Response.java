package com.fy.minicat.connector;

import com.fy.minicat.utils.StaticResourceUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Locale;

/**
 * @author fangyuan
 */

public class Response  implements HttpServletResponse {

    private SelectionKey sk;

    private ServletOutputStream minicatServletOutputStream;

    public Response() {
    }


    public Response(SelectionKey sk, ServletOutputStream minicatServletOutputStream) {
        this.sk = sk;
        this.minicatServletOutputStream = minicatServletOutputStream;
    }


    @Override
    public void addCookie(Cookie cookie) {

    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public String encodeURL(String url) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return null;
    }

    @Override
    public String encodeUrl(String url) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return null;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {

    }

    @Override
    public void sendError(int sc) throws IOException {

    }

    @Override
    public void sendRedirect(String location) throws IOException {

    }

    @Override
    public void setDateHeader(String name, long date) {

    }

    @Override
    public void addDateHeader(String name, long date) {

    }

    @Override
    public void setHeader(String name, String value) {

    }

    @Override
    public void addHeader(String name, String value) {

    }

    @Override
    public void setIntHeader(String name, int value) {

    }

    @Override
    public void addIntHeader(String name, int value) {

    }

    @Override
    public void setStatus(int sc) {

    }

    @Override
    public void setStatus(int sc, String sm) {

    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return minicatServletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return null;
    }

    @Override
    public void setCharacterEncoding(String charset) {

    }

    @Override
    public void setContentLength(int len) {

    }

    @Override
    public void setContentLengthLong(long len) {

    }

    @Override
    public void setContentType(String type) {

    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    /**
     * 写入404信息
     * @param message
     */
    public void write(String message) throws IOException {

        ByteBuffer bf = ByteBuffer.allocate(Http11Processor.writeBuffSize);
        bf.put(message.getBytes());
        bf.flip();
        ((SocketChannel) this.sk.channel()).write(bf);
        this.sk.cancel();
        SocketChannel socket = (SocketChannel) this.sk.attachment();
        socket.close();
    }

    /**
     *
     */
    public void outStatic(String url) throws Exception {
        // 获取静态资源文件的绝对路径
        String absoluteResourcePath = StaticResourceUtil.getAbsolutePath(url);

        // 输入静态资源文件
        File file = new File(absoluteResourcePath);
        if(file.exists() && file.isFile()) {
            // 读取静态资源文件，输出静态资源
            StaticResourceUtil.outputStaticResource(new FileInputStream(file), (SocketChannel) this.sk.channel());
            this.sk.cancel();
            SocketChannel socket = (SocketChannel) this.sk.attachment();
            socket.close();
        }else{
            // 输出404
            write(Http11Processor.getHttpHeader404());
        }
    }
}
