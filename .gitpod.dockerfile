FROM gitpod/workspace-full-vnc

RUN sudo apt-get update && \
    sudo apt-get install -y libgtk-3-dev && \
    sudo rm -rf /var/lib/apt/lists/*

#RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh \
#             && sdk install java 21.0.3-tem \
#             && sdk default java 21.0.3-tem"
