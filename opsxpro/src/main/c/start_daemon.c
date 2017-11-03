//
// Created by zheng li on 2017/10/31.
//

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>

#define PARSE_ENV 1
#define PARSE_ARGS 2

#ifdef NDEBUG
#define PRINT_LOG 0
#else
#define PRINT_LOG 1
#endif

int child_handle(int argc, char **argv) {
  char *args[argc - 1];
  args[argc - 1] = NULL;
  int curr = 0;
  int j = 0, term_len = 0;
  for (int i = 1; i < argc; ++i) {

    char *str = argv[i];

    printf("---arg ---   %s\n",str);

    if (strcmp(str, "--env") == 0) {
      curr = PARSE_ENV;
      term_len++;
      continue;
    } else if (strcmp(str, "--args") == 0) {
      curr = PARSE_ARGS;
      term_len++;
      continue;
    }
    switch (curr) {
      case PARSE_ENV:
        term_len++;

        //if (PRINT_LOG) {
          printf("env ---- %s\n", str);
        //}

        if (strlen(str) > 2) {
          char *r = strtok(str, ";");
          while (r != NULL) {

            size_t len = strlen(r);
            size_t idx = strcspn(r, "=");
            if (idx > 0 && idx < len) {
              char k[idx + 1], v[len - idx + 1];
              memcpy(k, r, idx);
              memcpy(v, r + idx + 1, len - idx);

              k[idx] = '\0';
              v[len - idx] = '\0';
              //if (PRINT_LOG) {
                printf("k=%s,v=%s   len=%zd,idx=%zd\n", k, v, len, idx);
              //}

              printf("setenv --> %s=%s", k, v);
              if(setenv(k, v, 1) == -1){
                printf("  ---- error\n");
              } else{
                printf("  ---- success\n");
              }
            }
            r = strtok(NULL, ";");

          }
        }

        break;
      case PARSE_ARGS:
        args[j] = calloc(strlen(str) + 1, sizeof(char));
        strcpy(args[j], str);
        j++;
        break;
      default:
        break;
    }

  }

  if (j < 0) {
    perror("args error");
    return EXIT_FAILURE;
  }

  char *nargs[j + 1];

  memcpy(nargs, args, j * sizeof(args[0]));
  nargs[j] = NULL;

  for (int i = 0; i < j+1; i++) {
    printf("%s ", nargs[i]);
  }
  printf("\n !!! execvp --- \n");

  if (execvp(nargs[0], nargs)) {
    perror("execv error ");
    return EXIT_FAILURE;
  }
  return 0;
}


int main(int argc, char **argv) {
  if (argc < 2) {
    perror("usage: [--env key1=value1;key2=value2;...] --args executable_file_path [args]");
    return EXIT_FAILURE;
  }

  printf("current uid: %d, pid:%d\n", getuid(), getpid());

  pid_t child_p = fork();

  printf("fork pid ---> %d \n",child_p);
  if (child_p == 0) {
    //child process
    //set to daemon

    signal(SIGINT, SIG_IGN);
    child_p = daemon(0, 1);
    if (child_p == -1) {
      perror("fork error set daemon\n");
      return EXIT_FAILURE;
    }

    printf("fork success! child pid:%d\n", getpid());

    return child_handle(argc, argv);

  } else if (child_p > 0) {
    signal(SIGCHLD, SIG_IGN);

    printf("fork parent \n");

    //current parent process

  } else {
    perror("fork fail !\n");
    exit(1);
  }

  return 0;
}