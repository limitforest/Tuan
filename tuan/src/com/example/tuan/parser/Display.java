package com.example.tuan.parser;

public class Display {
	String wap_url;

	String title;
	String small_image_url;
	String price;
	String rebate;
	String bought;
	String addr;
	public String getWap_url() {
		return wap_url;
	}
	public void setWap_url(String wap_url) {
		this.wap_url = wap_url;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSmall_image_url() {
		return small_image_url;
	}
	public void setSmall_image_url(String small_image_url) {
		this.small_image_url = small_image_url;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getRebate() {
		return rebate;
	}
	public void setRebate(String rebate) {
		this.rebate = rebate;
	}
	public String getBought() {
		return bought;
	}
	public void setBought(String bought) {
		this.bought = bought;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	@Override
	public String toString() {
		return "Display [wap_url=" + wap_url + ", title=" + title
				+ ", small_image_url=" + small_image_url + ", price=" + price
				+ ", rebate=" + rebate + ", bought=" + bought + ", addr="
				+ addr + "]";
	}
	
	
	
}
