package structure.configs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class FileConfig extends ConfigSection{
	private File file;
	private Yaml parser;
	public FileConfig(File file){
		super(new HashMap<String, Object>());
		this.file = file;
		System.out.println("Loading config: " + file.getAbsoluteFile());
		reload();
	}
	
	@SuppressWarnings("unchecked")
	public boolean reload(){
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		parser = new Yaml(options);
		try{
			InputStream in = new FileInputStream(file);
			map = (Map<String, Object>) parser.load(in);
		}
		catch(FileNotFoundException e){
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			catch (IOException e1) {
				e.printStackTrace();
				e1.printStackTrace();
			}
			return false;
		}
		if(map == null){
			map = new HashMap<String, Object>();
		}
		
		return true;
	}
	
	public void save(){
		if(!file.exists()){
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			}
			catch (IOException e) {
				System.out.println("Failed to create config file!");
				e.printStackTrace();
				return; //You dont exist, we can't write to you.
			}
		}
		PrintStream ps = null;
		try{
			ps = new PrintStream(file);
			String out = parser.dump(map); 
			ps.print(out);
			ps.close();
		}
		catch(FileNotFoundException e){
			System.out.println("Failed to save config file!");
			e.printStackTrace();
			return;
		}
	}
	
	@Override
	public String toString(){
		return parser.dump(map);
	}
}