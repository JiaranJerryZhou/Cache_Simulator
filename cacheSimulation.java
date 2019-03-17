import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.*;

public class cacheSimulation {
	final static int Data_Read = 0;
	final static int Data_Write = 1;
	final static int Inst_Fetch = 2; 
	final static int Random = 0;
	final static int LRU = 1;
	final static int write_allocate = 0;
	final static int write_no_allocate = 1;
	
	public cache[] L1cache;
	public cache[] L2cache;
	public boolean L1_unified, L2_unified;
	public int nlevels;
	public float L1_hitTime, L2_hitTime, DRAM_accessTime;
	
	public int nrecords;
	
	public cacheSimulation(int nlevels, boolean L1_unified, boolean L2_unified, String L1, String L2, float L1_hitTime, float L2_hitTime, float DRAM_accessTime){
		this.nlevels = nlevels;
		this.L1_unified = L1_unified;
		this.L1_hitTime = L1_hitTime;
		this.DRAM_accessTime = DRAM_accessTime;
		this.nrecords = 0;
		
		if(L1_unified){
			L1cache = new cache[1];
			String[] L1_configuration = L1.split(" ");
			int L1_A = Integer.parseInt(L1_configuration[0]);
			int L1_B = Integer.parseInt(L1_configuration[1]);
			int L1_C = Integer.parseInt(L1_configuration[2]);
			int L1_strategy = Integer.parseInt(L1_configuration[3]);
			int L1_writeScheme = Integer.parseInt(L1_configuration[4]);
			L1cache[0] = new cache(L1_A, L1_B, L1_C, L1_strategy, L1_writeScheme, 0);
		}else{
			L1cache = new cache[2]; //L1cache[0] is IMEM, L1cache[1] is DMEM, the same mechanism for L2
			String[] L1_configuration = L1.split(" ");
			int L1_I_A = Integer.parseInt(L1_configuration[0]);
			int L1_I_B = Integer.parseInt(L1_configuration[1]);
			int L1_I_C = Integer.parseInt(L1_configuration[2]);
			int L1_I_strategy = Integer.parseInt(L1_configuration[3]);
			int L1_I_writeScheme = Integer.parseInt(L1_configuration[4]);
			int L1_D_A = Integer.parseInt(L1_configuration[5]);
			int L1_D_B = Integer.parseInt(L1_configuration[6]);
			int L1_D_C = Integer.parseInt(L1_configuration[7]);
			int L1_D_strategy = Integer.parseInt(L1_configuration[8]);
			int L1_D_writeScheme = Integer.parseInt(L1_configuration[9]);
			L1cache[0] = new cache(L1_I_A, L1_I_B, L1_I_C, L1_I_strategy, L1_I_writeScheme, 0);
			L1cache[1] = new cache(L1_D_A, L1_D_B, L1_D_C, L1_D_strategy, L1_D_writeScheme, 0);
		}
		
		if(nlevels > 1){
			if(L2_unified){
				L2cache = new cache[1];
				String[] L2_configuration = L2.split(" ");
				int L2_A = Integer.parseInt(L2_configuration[0]);
				int L2_B = Integer.parseInt(L2_configuration[1]);
				int L2_C = Integer.parseInt(L2_configuration[2]);
				int L2_strategy = Integer.parseInt(L2_configuration[3]);
				int L2_writeScheme = Integer.parseInt(L2_configuration[4]);
				L2cache[0] = new cache(L2_A, L2_B, L2_C, L2_strategy, L2_writeScheme, 1);
			}else{
				L2cache = new cache[2];
				String[] L2_configuration = L2.split(" ");
				int L2_I_A = Integer.parseInt(L2_configuration[0]);
				int L2_I_B = Integer.parseInt(L2_configuration[1]);
				int L2_I_C = Integer.parseInt(L2_configuration[2]);
				int L2_I_strategy = Integer.parseInt(L2_configuration[3]);
				int L2_I_writeScheme = Integer.parseInt(L2_configuration[4]);
				int L2_D_A = Integer.parseInt(L2_configuration[5]);
				int L2_D_B = Integer.parseInt(L2_configuration[6]);
				int L2_D_C = Integer.parseInt(L2_configuration[7]);
				int L2_D_strategy = Integer.parseInt(L2_configuration[8]);
				int L2_D_writeScheme = Integer.parseInt(L2_configuration[9]);
				L2cache[0] = new cache(L2_I_A, L2_I_B, L2_I_C, L2_I_strategy, L2_I_writeScheme, 1);
				L2cache[1] = new cache(L2_D_A, L2_D_B, L2_D_C, L2_D_strategy, L2_D_writeScheme, 1);
			}
			this.L2_unified = L2_unified;
			this.L2_hitTime = L2_hitTime;
		}
	}
	
	public void process(String str){
//		System.out.println(str);
		String[] s = str.split(" ");
		int code = Integer.parseInt(s[0]);
		long address = Long.parseLong(s[1], 16);
//		System.out.println(code);
//		System.out.println(address);
		nrecords++;
		System.out.println(nrecords);
		
		if(code == Data_Read){
			if(L1_unified){
				Read(address, 0, L1cache[0]);
			}else{
				Read(address, 0, L1cache[1]);
			}
		}else if (code == Data_Write){
			if(L1_unified){
				Write(address, 0, L1cache[0]);
			}else{
				Write(address, 0, L1cache[1]);
			}
		}else{
			Fetch(address, 0, L1cache[0]);
		}		
	}
	
	public void Fetch(Long address, int level, cache C){
		C.nfetch++;
		long a = address / C.blockSize;
		long tag = a / C.nsets;
		long index = a % C.nsets;
		
		for(int col = 0; col < C.associativity; col++){
			cacheBlock data = C.myCache[(int) index][col];
			if(data.valid && data.tag == tag){
				C.nfetch_hit++;
				data.lastAccessTime = C.currentTime;
				break;
			}else if(C.associativity == 1){
				C.nfetch_miss++;
				if(level == 0 && nlevels > 1){
					Fetch(address, 1, L2cache[0]);
				}
				data.tag = tag;
				data.valid = true;
				data.dirty = false;
				data.lastAccessTime = C.currentTime;
				break;
			}else if (col == C.associativity - 1){
				C.nfetch_miss++;
				int col_evicted;
				cacheBlock data_evicted;
				// Pick random one to evict
				if(C.strategy == Random){
					col_evicted = getRandomIndex(C.associativity);
					data_evicted = C.myCache[(int) index][col_evicted];
				// LRU
				}else{
					col_evicted = getLRUIndex(C.myCache[(int) index]);
					data_evicted = C.myCache[(int) index][col_evicted];
				}
				if(level == 0 && nlevels > 1){
					Fetch(address, 1, L2cache[0]);
				}
				data_evicted.tag = tag;
				data_evicted.valid = true;
				data_evicted.dirty = false;
				data_evicted.lastAccessTime = C.currentTime;
			}
		}
		
		C.currentTime++;
	}
	
	public void Read(Long address, int level, cache C){
		C.nread++;
		long a = address / C.blockSize;
		long tag = a / C.nsets;
		long index = a % C.nsets;
		
		for(int col = 0; col < C.associativity; col++){
			cacheBlock data = C.myCache[(int) index][col];
			if(data.valid && data.tag == tag){
				C.nread_hit++;
				data.lastAccessTime = C.currentTime;
				break;
			}else if(C.associativity == 1){
				C.nread_miss++;
				if(data.valid && data.dirty && level == 0 && nlevels > 1){
					Long address_toWrite = (data.tag * C.nsets + index) * C.blockSize;
					if(L2_unified){
						Write(address_toWrite, 1, L2cache[0]);
					}else{
						Write(address_toWrite, 1, L2cache[1]);
					}	
				}
				if(level == 0 && nlevels > 1){
					if(L2_unified){
						Read(address, 1, L2cache[0]);
					}else{
						Read(address, 1, L2cache[1]);
					}	
				}
				data.tag = tag;
				data.valid = true;
				data.dirty = false;
				data.lastAccessTime = C.currentTime;
				break;
			}else if (col == C.associativity - 1){
				C.nread_miss++;
				int col_evicted;
				cacheBlock data_evicted;
				// Pick random one to evict
				if(C.strategy == Random){
					col_evicted = getRandomIndex(C.associativity);
					data_evicted = C.myCache[(int) index][col_evicted];
					if(data_evicted.valid && data_evicted.dirty && level == 0 && nlevels > 1){
						Long address_toWrite = (data_evicted.tag * C.nsets + index) * C.blockSize;
						if(L2_unified){
							Write(address_toWrite, 1, L2cache[0]);
						}else{
							Write(address_toWrite, 1, L2cache[1]);
						}	
					}
				// LRU
				}else{
					col_evicted = getLRUIndex(C.myCache[(int) index]);
					data_evicted = C.myCache[(int) index][col_evicted];
					if(data_evicted.valid && data_evicted.dirty && level == 0 && nlevels > 1){
						Long address_toWrite = (data_evicted.tag * C.nsets + index) * C.blockSize;
						if(L2_unified){
							Write(address_toWrite, 1, L2cache[0]);
						}else{
							Write(address_toWrite, 1, L2cache[1]);
						}	
					}
				}
				if(level == 0 && nlevels > 1){
					if(L2_unified){
						Read(address, 1, L2cache[0]);
					}else{
						Read(address, 1, L2cache[1]);
					}	
				}
				data_evicted.tag = tag;
				data_evicted.valid = true;
				data_evicted.dirty = false;
				data_evicted.lastAccessTime = C.currentTime;				
			}
		}
		
		C.currentTime++;
	}
	
	public void Write(Long address, int level, cache C){
		C.nwrite++;
		long a = address / C.blockSize;
		long tag = a / C.nsets;
		long index = a % C.nsets;
		
		for(int col = 0; col < C.associativity; col++){
			cacheBlock data = C.myCache[(int) index][col];
			// write hit
			if(data.valid && data.tag == tag){
				C.nwrite_hit++;
				data.dirty = true;
				data.lastAccessTime = C.currentTime;
				break;
			// write miss and direct-mapped
			}else if(C.associativity == 1){
				C.nwrite_miss++;
				if(C.writeScheme == write_allocate){
					if(data.valid && data.dirty && level == 0 && nlevels > 1){
						Long address_toWrite = (data.tag * C.nsets + index) * C.blockSize;
						if(L2_unified){
							Write(address_toWrite, 1, L2cache[0]);
						}else{
							Write(address_toWrite, 1, L2cache[1]);
						}	
					}
					if(level == 0 && nlevels > 1){
						if(L2_unified){
							Read(address, 1, L2cache[0]);
						}else{
							Read(address, 1, L2cache[1]);
						}	
					}
					data.tag = tag;
					data.valid = true;
					data.dirty = true;
					data.lastAccessTime = C.currentTime;
				}else{
					if(nlevels > 1){
						if(L2_unified){
							Write(address, 1, L2cache[0]);
						}else{
							Write(address, 1, L2cache[1]);
						}
					}
				}		
				break;
			// write miss and not direct-mapped
			}else if(col == C.associativity - 1){
				C.nwrite_miss++;
				int col_evicted;
				cacheBlock data_evicted;
				if(C.writeScheme == 0){
					if(C.strategy == Random){
						col_evicted = getRandomIndex(C.associativity);
						data_evicted = C.myCache[(int) index][col_evicted];
						if(data_evicted.valid && data_evicted.dirty && level == 0 && nlevels > 1){
							Long address_toWrite = (data_evicted.tag * C.nsets + index) * C.blockSize;
							if(L2_unified){
								Write(address_toWrite, 1, L2cache[0]);
							}else{
								Write(address_toWrite, 1, L2cache[1]);
							}	
						}
						if(level == 0 && nlevels > 1){
							if(L2_unified){
								Read(address, 1, L2cache[0]);
							}else{
								Read(address, 1, L2cache[1]);
							}	
						}
					}else{
						col_evicted = getLRUIndex(C.myCache[(int) index]);
						data_evicted = C.myCache[(int) index][col_evicted];
						if(data_evicted.valid && data_evicted.dirty && level == 0 && nlevels > 1){
							Long address_toWrite = (data_evicted.tag * C.nsets + index) * C.blockSize;
							if(L2_unified){
								Write(address_toWrite, 1, L2cache[0]);
							}else{
								Write(address_toWrite, 1, L2cache[1]);
							}	
						}
						if(level == 0 && nlevels > 1){
							if(L2_unified){
								Read(address, 1, L2cache[0]);
							}else{
								Read(address, 1, L2cache[1]);
							}	
						}
					}
					data_evicted.tag = tag;
					data_evicted.valid = true;
					data_evicted.dirty = true;
					data_evicted.lastAccessTime = C.currentTime;
				}else{
					if(level == 0 && nlevels > 1){
						if(L2_unified){
							Write(address, 1, L2cache[0]);
						}else{
							Write(address, 1, L2cache[1]);
						}	
					}
				}
			}
		}
		
		C.currentTime++;
	}
	
	public int getRandomIndex(int size){
		int index = (int) (Math.random() * size);
		return index;
	}
	
	public int getLRUIndex(cacheBlock[] C){
		int LRU = Integer.MAX_VALUE;
		int index = 0;
		
		for(int i = 0; i < C.length; i++){
			cacheBlock data = C[i];
			if(data.lastAccessTime < LRU){
				LRU = data.lastAccessTime;
				index = i;
			}
		}
		
		return index;
	}
	
	public void printResult(){
		int InstrnDemandFetch = L1cache[0].nfetch;
		int InstrnDemandMiss = L1cache[0].nfetch_miss;
		float InstrnDemandMissRate = InstrnDemandMiss / (float) InstrnDemandFetch;
			
		int totalDemandFetches_L1, totalDemandMiss_L1, DataDemandFetch, DataDemandMiss, DataReadDemandFetch, DataReadDemandMiss, DataWriteDemandFetch, DataWriteDemandMiss;
		float totalDemandMissRate_L1, DataDemandMissRate, DataReadDemandMissRate, DataWriteDemandMissRate;
		
		if(L1_unified){			
			totalDemandFetches_L1 = L1cache[0].nfetch + L1cache[0].nread + L1cache[0].nwrite;			
			totalDemandMiss_L1 = L1cache[0].nfetch_miss + L1cache[0].nread_miss + L1cache[0].nwrite_miss;
			totalDemandMissRate_L1 = totalDemandMiss_L1 / (float) totalDemandFetches_L1;
			
			DataDemandFetch = L1cache[0].nread + L1cache[0].nwrite;
			DataDemandMiss = L1cache[0].nread_miss + L1cache[0].nwrite_miss;
			DataDemandMissRate = DataDemandMiss / (float) DataDemandFetch;
			
			DataReadDemandFetch = L1cache[0].nread;
			DataReadDemandMiss = L1cache[0].nread_miss;
			DataReadDemandMissRate = DataReadDemandMiss / (float) DataReadDemandFetch;
			
			DataWriteDemandFetch = L1cache[0].nwrite;
			DataWriteDemandMiss = L1cache[0].nwrite_miss;
			DataWriteDemandMissRate = DataWriteDemandMiss / (float) DataWriteDemandFetch; 			
		}else{
			totalDemandFetches_L1 = L1cache[0].nfetch + L1cache[1].nread + L1cache[1].nwrite;			
			totalDemandMiss_L1 = L1cache[0].nfetch_miss + L1cache[1].nread_miss + L1cache[1].nwrite_miss;
			totalDemandMissRate_L1 = totalDemandMiss_L1 / (float) totalDemandFetches_L1;
			
			DataDemandFetch = L1cache[1].nread + L1cache[1].nwrite;
			DataDemandMiss = L1cache[1].nread_miss + L1cache[1].nwrite_miss;
			DataDemandMissRate = DataDemandMiss / (float) DataDemandFetch;
			
			DataReadDemandFetch = L1cache[1].nread;
			DataReadDemandMiss = L1cache[1].nread_miss;
			DataReadDemandMissRate = DataReadDemandMiss / (float) DataReadDemandFetch;
			
			DataWriteDemandFetch = L1cache[1].nwrite;
			DataWriteDemandMiss = L1cache[1].nwrite_miss;
			DataWriteDemandMissRate = DataWriteDemandMiss / (float) DataWriteDemandFetch; 
		}
		
		System.out.println(totalDemandFetches_L1 + " demand fetches for L1 cache");
		System.out.println(totalDemandMiss_L1 + " demand misses for L1 cache");
		System.out.println("The total demand miss rate for L1 cache is " + totalDemandMissRate_L1);
		System.out.println();
		
		System.out.println("Instruction cache level 1:");
		System.out.println(InstrnDemandFetch + " times of instruction demand fetches");
		System.out.println(InstrnDemandMiss + " times of instruction demand misses");
		System.out.println("The instruction demand miss rate is " + InstrnDemandMissRate);
		System.out.println();
		
		System.out.println("Data cache level 1: ");
		System.out.println(DataDemandFetch + " times of data demand fetches");
		System.out.println(DataDemandMiss + " times of data demand misses");
		System.out.println("The data demand miss rate is " + DataDemandMissRate);
		System.out.println();
		
		System.out.println(DataReadDemandFetch + " times of data reads");
		System.out.println(DataReadDemandMiss + " times of data read misses");
		System.out.println("The data read miss rate is " + DataReadDemandMissRate);
		System.out.println();
		
		System.out.println(DataWriteDemandFetch + " times of data writes");
		System.out.println(DataWriteDemandMiss + " times of data write misses");
		System.out.println("The data write miss rate is " + DataWriteDemandMissRate);
		System.out.println();
		
		
		int totalDemandFetches_L2, totalDemandMiss_L2, InstrnDemandFetch_L2, InstrnDemandMiss_L2, DataDemandFetch_L2, DataDemandMiss_L2, DataReadDemandFetch_L2, DataReadDemandMiss_L2, DataWriteDemandFetch_L2, DataWriteDemandMiss_L2;
		float totalDemandMissRate_L2, InstrnDemandMissRate_L2, DataDemandMissRate_L2, DataReadDemandMissRate_L2, DataWriteDemandMissRate_L2;
		
		if(nlevels > 1){
			InstrnDemandFetch_L2 = L2cache[0].nfetch;
			InstrnDemandMiss_L2 = L2cache[0].nfetch_miss;
			InstrnDemandMissRate_L2 = InstrnDemandMiss_L2 / (float) InstrnDemandFetch_L2;
			
			if(L2_unified){
				totalDemandFetches_L2 = L2cache[0].nfetch + L2cache[0].nread + L2cache[0].nwrite;
				totalDemandMiss_L2 = L2cache[0].nfetch_miss +  L2cache[0].nread_miss + L2cache[0].nwrite_miss;
				totalDemandMissRate_L2 = totalDemandMiss_L2 / (float) totalDemandFetches_L2;
				
				DataDemandFetch_L2 = L2cache[0].nread + L2cache[0].nwrite;
				DataDemandMiss_L2 = L2cache[0].nread_miss + L2cache[0].nwrite_miss;
				DataDemandMissRate_L2 = DataDemandMiss_L2 / (float) DataDemandFetch_L2;
				
				DataReadDemandFetch_L2 = L2cache[0].nread;
				DataReadDemandMiss_L2 = L2cache[0].nread_miss;
				DataReadDemandMissRate_L2 = DataReadDemandMiss_L2 / (float) DataReadDemandFetch_L2;
				
				DataWriteDemandFetch_L2 = L2cache[0].nwrite;
				DataWriteDemandMiss_L2 = L2cache[0].nwrite_miss;
				DataWriteDemandMissRate_L2 = DataWriteDemandMiss_L2 / (float) DataWriteDemandFetch_L2; 
			}else{
				totalDemandFetches_L2 = L2cache[0].nfetch + L2cache[1].nread + L2cache[1].nwrite;
				totalDemandMiss_L2 = L2cache[0].nfetch_miss +  L2cache[1].nread_miss + L2cache[1].nwrite_miss;
				totalDemandMissRate_L2 = totalDemandMiss_L2 / (float) totalDemandFetches_L2;
				
				DataDemandFetch_L2 = L2cache[1].nread + L2cache[1].nwrite;
				DataDemandMiss_L2 = L2cache[1].nread_miss + L2cache[1].nwrite_miss;
				DataDemandMissRate_L2 = DataDemandMiss_L2 / (float) DataDemandFetch_L2;
				
				DataReadDemandFetch_L2 = L2cache[1].nread;
				DataReadDemandMiss_L2 = L2cache[1].nread_miss;
				DataReadDemandMissRate_L2 = DataReadDemandMiss_L2 / (float) DataReadDemandFetch_L2;
		
				DataWriteDemandFetch_L2 = L2cache[1].nwrite;
				DataWriteDemandMiss_L2 = L2cache[1].nwrite_miss;
				DataWriteDemandMissRate_L2 = DataWriteDemandMiss_L2 / (float) DataWriteDemandFetch_L2; 
			}
			System.out.println(totalDemandFetches_L2 + " demand fetches for L12 cache");
			System.out.println(totalDemandMiss_L2 + " demand misses for L2 cache");
			System.out.println("The total demand miss rate for L2 cache is " + totalDemandMissRate_L1);
			System.out.println();
			
			System.out.println("Instruction cache level 2:");
			System.out.println(InstrnDemandFetch_L2 + " times of instruction demand fetches");
			System.out.println(InstrnDemandMiss_L2 + " times of instruction demand misses");
			System.out.println("The instruction demand miss rate is " + InstrnDemandMissRate_L2);
			System.out.println();
			
			System.out.println("Data cache level 2: ");
			System.out.println(DataDemandFetch_L2 + " times of data demand fetches");
			System.out.println(DataDemandMiss_L2 + " times of data demand misses");
			System.out.println("The data demand miss rate is " + DataDemandMissRate_L2);
			System.out.println();
			
			System.out.println(DataReadDemandFetch_L2 + " times of data reads");
			System.out.println(DataReadDemandMiss_L2 + " times of data read misses");
			System.out.println("The data read miss rate is " + DataReadDemandMissRate_L2);
			System.out.println();
			
			System.out.println(DataWriteDemandFetch_L2 + " times of data writes");
			System.out.println(DataWriteDemandMiss_L2 + " times of data write misses");
			System.out.println("The data write miss rate is " + DataWriteDemandMissRate_L2);
			System.out.println();
		}
		
		int demandFetch_L1, demandMiss_L1, demandFetch_L2, demandMiss_L2;
		float demandMissRate_L1, demandMissRate_L2;
		float accessTime_L1, accessTime_L2;
		
		if(nlevels == 1){
			if(L1_unified){
				demandFetch_L1 = L1cache[0].nfetch + L1cache[0].nread + L1cache[0].nwrite;
				demandMiss_L1 = L1cache[0].nfetch_miss + L1cache[0].nread_miss + L1cache[0].nwrite_miss;
			}else{
				demandFetch_L1 = L1cache[0].nfetch + L1cache[1].nread + L1cache[1].nwrite;
				demandMiss_L1 = L1cache[0].nfetch_miss + L1cache[1].nread_miss + L1cache[1].nwrite_miss;
			}
			demandMissRate_L1 = demandMiss_L1 / (float) demandFetch_L1;
			accessTime_L1 = L1_hitTime + demandMissRate_L1 * DRAM_accessTime;
			System.out.println("The average access time for L1 is " + accessTime_L1);
		}else{
			if(L1_unified){
				demandFetch_L1 = L1cache[0].nfetch + L1cache[0].nread + L1cache[0].nwrite;
				demandMiss_L1 = L1cache[0].nfetch_miss + L1cache[0].nread_miss + L1cache[0].nwrite_miss;
			}else{
				demandFetch_L1 = L1cache[0].nfetch + L1cache[1].nread + L1cache[1].nwrite;
				demandMiss_L1 = L1cache[0].nfetch_miss + L1cache[1].nread_miss + L1cache[1].nwrite_miss;
			}
			
			if(L2_unified){
				demandFetch_L2 = L2cache[0].nfetch + L2cache[0].nread + L2cache[0].nwrite;
				demandMiss_L2 = L2cache[0].nfetch_miss + L2cache[0].nread_miss + L2cache[0].nwrite_miss;
			}else{
				demandFetch_L2 = L2cache[0].nfetch + L2cache[1].nread + L2cache[1].nwrite;
				demandMiss_L2 = L2cache[0].nfetch_miss + L2cache[1].nread_miss + L2cache[1].nwrite_miss;
			}
			
			demandMissRate_L1 = demandMiss_L1 / (float) demandFetch_L1;
			demandMissRate_L2 = demandMiss_L2 / (float) demandFetch_L2;
			accessTime_L2 = L2_hitTime + demandMissRate_L2 * DRAM_accessTime;
			accessTime_L1 = L1_hitTime + demandMissRate_L1 * accessTime_L2;
			System.out.println("The average access time for L1 is " + accessTime_L1);
			System.out.println("The average access time for L2 is " + accessTime_L2);
		}
	}
	
	/** Get CPU time in nanoseconds. */
	public static long getCpuTime( ) {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    return bean.isCurrentThreadCpuTimeSupported( ) ?
	        bean.getCurrentThreadCpuTime( ) : 0L;
	}
	
	public static void main(String[] args) throws IOException{
//		URL path = new URL("http://people.ee.duke.edu/~jab/ece550/homeworks/spec026.ucomp.din.10000");
		URL path = new URL("http://people.ee.duke.edu/~jab/ece550/homeworks/spec026.ucomp.din");
	    BufferedReader in = new BufferedReader(new InputStreamReader(path.openStream()));
	    String inputLine;
	    long startWallclockTime = System.nanoTime();
	    long startCpuTimeNano   = getCpuTime( );
//		cacheSimulation cs = new cacheSimulation(1, true, true, "1 32 8192 0 0", "", 0, 0, 0);
		cacheSimulation cs = new cacheSimulation(2, false, true, "2 64 32768 0 1 2 64 32768 0 1", "8 64 524288 1 1", 0, 0, 0);
		while((inputLine = in.readLine()) != null){
			cs.process(inputLine);
		}
		double taskWallclockTime = (System.nanoTime() - startWallclockTime) / 1000000000.0;
		double taskCpuTimeNano  = (getCpuTime( ) - startCpuTimeNano) / 1000000000.0;
		System.out.println("Wallclock Time: " + taskWallclockTime);
		System.out.println("CPU Time: " + taskCpuTimeNano);
		cs.printResult();
		in.close();
	}
}
