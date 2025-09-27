# Use official Maven image with Eclipse Temurin Java 25
FROM maven:3.9.11-eclipse-temurin-25

# Install unzip, zsh and other tools needed
RUN apt-get update && apt-get install -y \
    unzip \
    zsh \
    git \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Install Claude Code
RUN curl -fsSL https://claude.ai/install.sh | bash

# Install AWS CLI
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" \
    && unzip awscliv2.zip \
    && ./aws/install \
    && rm -rf awscliv2.zip aws

# Install GitHub CLI using direct binary download to avoid repository issues
RUN ARCH=$(uname -m) && \
    if [ "$ARCH" = "x86_64" ]; then \
        GH_ARCH="linux_amd64"; \
    elif [ "$ARCH" = "aarch64" ]; then \
        GH_ARCH="linux_arm64"; \
    else \
        echo "Unsupported architecture: $ARCH" && exit 1; \
    fi && \
    GH_VERSION=$(curl -s https://api.github.com/repos/cli/cli/releases/latest | grep 'tag_name' | cut -d'"' -f4 | cut -c2-) && \
    curl -L "https://github.com/cli/cli/releases/download/v${GH_VERSION}/gh_${GH_VERSION}_${GH_ARCH}.tar.gz" -o gh.tar.gz && \
    tar -xzf gh.tar.gz && \
    mv "gh_${GH_VERSION}_${GH_ARCH}/bin/gh" /usr/local/bin/gh && \
    chmod +x /usr/local/bin/gh && \
    rm -rf gh.tar.gz "gh_${GH_VERSION}_${GH_ARCH}"

# Install Oh My Zsh and configure zsh as default shell
RUN sh -c "$(curl -fsSL https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)" "" --unattended && \
    chsh -s $(which zsh) root

# Set up shell history persistence for both bash and zsh
RUN mkdir -p /root/.shell_history && \
    # Bash configuration
    echo 'export HISTFILE=/root/.shell_history/.bash_history' >> /root/.bashrc && \
    echo 'export HISTSIZE=10000' >> /root/.bashrc && \
    echo 'export HISTFILESIZE=10000' >> /root/.bashrc && \
    echo 'export HISTCONTROL=ignoredups:erasedups' >> /root/.bashrc && \
    echo 'shopt -s histappend' >> /root/.bashrc && \
    echo 'export PROMPT_COMMAND="history -a; history -c; history -r"' >> /root/.bashrc && \
    # Zsh configuration
    echo 'export HISTFILE=/root/.shell_history/.zsh_history' >> /root/.zshrc && \
    echo 'export HISTSIZE=10000' >> /root/.zshrc && \
    echo 'export SAVEHIST=10000' >> /root/.zshrc && \
    echo 'setopt HIST_IGNORE_DUPS' >> /root/.zshrc && \
    echo 'setopt HIST_IGNORE_ALL_DUPS' >> /root/.zshrc && \
    echo 'setopt HIST_SAVE_NO_DUPS' >> /root/.zshrc && \
    echo 'setopt SHARE_HISTORY' >> /root/.zshrc

# Maven images typically run as root by default, which is fine for devcontainers