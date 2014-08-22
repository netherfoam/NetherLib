package structure.configs;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigSection{
	protected Map<String, Object> map;
	public ConfigSection(Map<String, Object> values){
		if(values == null){
			throw new NullPointerException("Config section may not have a null map.");
		}
		
		this.map = values;
	}
	
	@SuppressWarnings("unchecked")
	public void set(String s, Object o){
		if(s == null){
			throw new NullPointerException("Key may not be null.");
		}
		
		String[] parts = s.split("\\.");
		
		Map<String, Object> last = map;
		for(int i = 0; i < parts.length - 1; i++){
			Object q = last.get(parts[i]);
			
			if(q == null || q instanceof Map == false){
				q = new HashMap<String, Object>();
				last.put(parts[i], q);
			}
			last = (Map<String, Object>) q;
		}
		if(o instanceof ConfigSerializable){
			ConfigSerializable cs = (ConfigSerializable) o;
			ConfigSection m = new ConfigSection(new HashMap<String, Object>());
			cs.serialize(m);
			last.put(parts[parts.length - 1], m);
		}
		else{
			last.put(parts[parts.length - 1], o);
		}
	}
	
	public Set<String> getKeys(){
		return map.keySet();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ConfigSerializable>T getSerialized(String key, Class<T> clazz, T fallback){
		ConfigSection map = new ConfigSection((Map<String, Object>) getObject(key));
		try {
			try{
				Method m = clazz.getMethod("deserialize", ConfigSection.class);
				return (T) m.invoke(null, map);
			}
			catch(NoSuchMethodException ex){
				return clazz.getConstructor(ConfigSection.class).newInstance(map);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return fallback;
	}
	public <T extends ConfigSerializable>T getSerialized(String key, Class<T> clazz){
		return getSerialized(key, clazz, null);
	}
	
	public ConfigSection getSection(String key, ConfigSection fallback){
		Object o = getObject(key);
		try{
			if(o != null && o instanceof Map){
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) o;
				return new ConfigSection(map);
			}
		}
		catch(Exception e){}
		return fallback;
	}
	
	@SuppressWarnings("unchecked")
	public Object getObject(String s){
		if(s == null){
			throw new NullPointerException("Key may not be null.");
		}
		
		String[] parts = s.split("\\.");
		
		Map<String, Object> last = map;
		for(int i = 0; i < parts.length - 1; i++){
			Object q = last.get(parts[i]);
			if(q == null  || q instanceof Map == false){
				return null;
			}
			last = (Map<String, Object>) q;
		}
		return last.get(parts[parts.length - 1]);
	}
	public int getInt(String k){
		return getInt(k, 0);
	}
	public int getInt(String k, int fallback){
		try{
			return (int) getObject(k);
		}
		catch(Exception e){
			return fallback;
		}
	}
	public long getLong(String k){
		return getLong(k, 0);
	}
	public long getLong(String k, long fallback){
		try{
			return (long) getObject(k);
		}
		catch(Exception e){
			return fallback;
		}
	}
	public double getDouble(String k){
		return getDouble(k, 0);
	}
	public double getDouble(String k, double fallback){
		try{
			Object o = getObject(k);
			if(o instanceof Integer){
				return (int) o;
			}
			else if(o instanceof Long){
				return (long) o;
			}
			else if(o instanceof Double){
				return (double) o;
			}
			return (double) o;
		}
		catch(Exception e){
			return fallback;
		}
	}
	public String getString(String k){
		return getString(k, null);
	}
	public String getString(String k, String fallback){
		try{
			return getObject(k).toString();
		}
		catch(Exception e){
			return fallback;
		}
	}
	public boolean getBoolean(String k){
		return getBoolean(k, false);
	}
	public boolean getBoolean(String k, boolean fallback){
		try{
			Object o = getObject(k);
			if(o instanceof Boolean){
				return (boolean) o;
			}
			else if(o instanceof Integer){
				return ((int) o) != 0;
			}
			else if(o instanceof Double){
				return ((double) o) != 0;
			}
			else if(o instanceof Long){
				return ((long) o) != 0;
			}
			return (boolean) o;
		}
		catch(Exception e){
			return fallback;
		}
	}
}