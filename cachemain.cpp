#include <stdlib.h>
#include <string.h>
#include <time.h>

#include <cstdio>
#include <cstdlib>
#include <fstream>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>

#include "cache.h"
#include "cache_simu.h"
using namespace std;
int main(int argc, char ** argv) {
  ifstream inFile1;
  //ifstream inFile2;

  //   inFile1.open("Dinero10000.din");
  inFile1.open("DineroFull.din");
  string line;
   //cache_simu * cs2 =                                                                      
    //new cache_simu(1, true, true, "1 32 8192 0 0", "", 0, 0, 0); 
  if (!strcmp(argv[1], "demo1")) {
    //levels_number L1_u L2_u L1(A B C r0/LRU1 wa0/wna1) L2(A B C r0/LRU1 wa0/wna1) L1_h L2_h D_a
    //                              I        D

    cache_simu * cs1 = new cache_simu(1, true, true, "1 32 8192 0 1", "", 0, 0, 0);
    while (getline(inFile1, line)) {
      cs1->parse(line);
    }
    cs1->print();
  }
  else if (!strcmp(argv[1], "demo2")) {
    cache_simu * cs2 =
        new cache_simu(2, false, true, "1 32 8192 1 0 2 32 8192 0 0", "8 32 32768 0 0", 0, 0, 0);
    while (getline(inFile1, line)) {
      cs2->parse(line);
    }
    cs2->print();
  }
  else if (!strcmp(argv[1], "demo3")) {
    cache_simu * cs3 = new cache_simu(
        2, false, false, "1 64 8192 1 0 4 64 16384 1 0", "8 64 32768 1 0 16 64 65536 1 0", 0, 0, 0);
    while (getline(inFile1, line)) {
      cs3->parse(line);
    }
    cs3->print();
  }  //cache_simu * cs1 =
  //  new cache_simu(2, false, true, "1 32 16384 1 0 1 32 16384 1 0", "4 64 262144 1 0", 0, 0, 0);

  // cs2->print();
  //inFile1.close();
  inFile1.close();
}
