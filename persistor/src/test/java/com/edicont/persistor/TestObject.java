package com.edicont.persistor;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.edicont.persistor.annotations.Indexed;
import com.edicont.persistor.annotations.Uuid;


public class TestObject {

	private String firstname;
	private String lastname;
	@Indexed
	private int size;
	private Date dob;
	
	private String[] strings;
	
	private List<Integer> numbers;
	
	private TestSubObject testSub;
	
	private TestSubObject[] testSubArray;
	
	private List<TestSubObject> testSubList;
	
	@Uuid
	private String id;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	
	public Date getDob() {
		return dob;
	}
	public void setDob(Date dob) {
		this.dob = dob;
	}
	
	public String[] getStrings() {
		return strings;
	}
	public void setStrings(String[] strings) {
		this.strings = strings;
	}
	
	
	
	
	public TestSubObject[] getTestSubArray() {
		return testSubArray;
	}
	public void setTestSubArray(TestSubObject[] testSubArray) {
		this.testSubArray = testSubArray;
	}
	public TestSubObject getTestSub() {
		return testSub;
	}
	public void setTestSub(TestSubObject testSub) {
		this.testSub = testSub;
	}
	public List<Integer> getNumbers() {
		return numbers;
	}
	public void setNumbers(List<Integer> numbers) {
		this.numbers = numbers;
	}
	
	public List<TestSubObject> getTestSubList() {
		return testSubList;
	}
	public void setTestSubList(List<TestSubObject> testSubList) {
		this.testSubList = testSubList;
	}
	@Override
	public String toString() {
		return "TestObject [firstname=" + firstname + ", lastname=" + lastname
				+ ", size=" + size + ", dob=" + dob + ", strings="
				+ Arrays.toString(strings) + ", numbers=" + numbers
				+ ", testSub=" + testSub + ", testSubArray="
				+ Arrays.toString(testSubArray) + ", testSubList="
				+ testSubList + ", id=" + id + "]";
	}
	
	
}
