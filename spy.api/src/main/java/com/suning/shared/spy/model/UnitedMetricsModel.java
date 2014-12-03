package com.suning.shared.spy.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


import com.suning.shared.spy.enums.MetricsType;

/**
 * 类UnitedMetricsModel.java的实现描述
 * 
 * @author karry 2014-10-29 下午2:35:47
 */
public class UnitedMetricsModel implements Serializable {

	private static final long serialVersionUID = -2283555842100914153L;

	public Map<String, MetricsInfoModel> metricsModels = new HashMap<String, MetricsInfoModel>(MetricsType.size());

	public MetricsTarget metricsTarget;

	public Map<String, MetricsInfoModel> getMetricsModels() {
		return metricsModels;
	}
	

	public CpuInfoModel getCpuInfoModel() {
		return (CpuInfoModel) metricsModels.get(MetricsType.CPU.getName());
	}

	public boolean hasCpuInfo() {
		return metricsModels.get(MetricsType.CPU.getName())!=null;
	}

	public VmMemInfoModel getMemInfoModel() {
		return (VmMemInfoModel) metricsModels.get(MetricsType.MEM.getName());
	}

	public boolean hasMemInfo() {
		return metricsModels.get(MetricsType.MEM.getName())!=null;
	}

	public DiskInfoModel getDiskInfoModel() {
		return (DiskInfoModel) metricsModels.get(MetricsType.DISK.getName());
	}

	public boolean hasDiskInfo() {
		return metricsModels.get(MetricsType.DISK.getName())!=null;
	}

	public NetInfoModel getNetInfoModel() {
		return (NetInfoModel) metricsModels.get(MetricsType.NET.getName());
	}

	public boolean hasNetInfo() {
		return metricsModels.get(MetricsType.NET.getName())!=null;
	}

	public MetricsTarget getMetricsTarget() {
		return this.metricsTarget;
	}

	public AggregateGCInfoModel getGCInfoModel() {
		return (AggregateGCInfoModel) metricsModels.get(MetricsType.GC.getName());
	}

	public boolean hasGCInfo() {
		return metricsModels.get(MetricsType.GC.getName())!=null;
	}

	public AggregateHeapInfoModel getHeapInfoModel() {
		return (AggregateHeapInfoModel) metricsModels.get(MetricsType.HEAP.getName());
	}

	public boolean hasHeapInfo() {
		return metricsModels.get(MetricsType.HEAP.getName())!=null;
	}

	// //////////////////write//////////////////////

	public UnitedMetricsModel() {

	}

	public UnitedMetricsModel(MetricsTarget metricsTarget) {
		this.metricsTarget = metricsTarget;
	}

	public void setMetricsModel(MetricsInfoModel model) {
		metricsModels.put(model.getType().getName(), model);
	}

	public void clear() {
		metricsModels.clear();
	}

	public int size() {
		return metricsModels.size();
	}

	@Override
	public String toString() {
		return this.metricsTarget.getAppName() + "/" + this.metricsTarget.getClusterName() + "/" + this.metricsTarget.getIp();
	}
	
	public static void main(String[] args) throws Exception {
		UnitedMetricsModel m = new UnitedMetricsModel(new MetricsTarget("dddd","eeee","fffff"));
		
		m.setMetricsModel(new CpuInfoModel());
		
//		String json = JSON.toJSONString(m, SerializerFeature.WriteClassName);
//		UnitedMetricsModel n = JSON.parseObject(json, UnitedMetricsModel.class);
//		System.out.println(n);
//		JsonConfig jc = new JsonConfig();
//		jc.setRootClass(UnitedMetricsModel.class);
//		String json1 = net.sf.json.JSONObject.fromObject(m,jc).toString();
//		System.out.println(json1);
//		net.sf.json.JSONObject jo = net.sf.json.JSONObject.fromObject(json1);
//		UnitedMetricsModel m1 = (UnitedMetricsModel)net.sf.json.JSONObject.toBean(jo, UnitedMetricsModel.class);
//		System.out.println(m1);
		
//		Gson gson = new Gson();
//		String json2 = gson.toJson(m);
//		System.out.println(json2);
//		UnitedMetricsModel m2 = gson.fromJson(json2, UnitedMetricsModel.class);
//		System.out.println(m2);
		byte[] bs = serialize(m);
		UnitedMetricsModel obj = deserialize(bs);
		
		System.out.println(obj);

	}
	public static byte[] serialize(Object obj){ 
		ByteArrayOutputStream bos = null;
		ObjectOutputStream out = null;
		byte[] bytes = null;
		try {
			bos = new ByteArrayOutputStream();
			out = new ObjectOutputStream(bos);
			out.writeObject(obj);
			bytes = bos.toByteArray();
		}catch (IOException e) {
			
		}finally {
			if (null != out) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		
		return bytes;
	}
	
	public static UnitedMetricsModel deserialize(byte[] bytes) throws IOException, ClassNotFoundException{   
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream in = new ObjectInputStream(bis);
		
		Object obj=in.readObject();             
		in.close();
		bis.close();

		return (UnitedMetricsModel)obj;
	}   	
}
