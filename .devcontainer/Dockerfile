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
RUN curl -fsSL https://claude.ai/install.sh | bash && \
    # Ensure claude is in PATH
    if [ -f ~/.local/bin/claude ]; then ln -sf ~/.local/bin/claude /usr/local/bin/claude; fi && \
    if [ -f ~/bin/claude ]; then ln -sf ~/bin/claude /usr/local/bin/claude; fi && \
    # Add common binary paths to PATH
    echo 'export PATH="$HOME/.local/bin:$HOME/bin:$PATH"' >> /root/.bashrc && \
    echo 'export PATH="$HOME/.local/bin:$HOME/bin:$PATH"' >> /root/.zshrc

# Install Docker CLI using official Docker repository
RUN install -m 0755 -d /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc && \
    chmod a+r /etc/apt/keyrings/docker.asc && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null && \
    apt-get update && \
    apt-get install -y docker-ce-cli && \
    rm -rf /var/lib/apt/lists/*

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
    echo 'export PATH="$HOME/.local/bin:$HOME/bin:$PATH"' >> /root/.zshrc && \
    echo 'export HISTFILE=/root/.shell_history/.zsh_history' >> /root/.zshrc && \
    echo 'export HISTSIZE=10000' >> /root/.zshrc && \
    echo 'export SAVEHIST=10000' >> /root/.zshrc && \
    echo 'setopt HIST_IGNORE_DUPS' >> /root/.zshrc && \
    echo 'setopt HIST_IGNORE_ALL_DUPS' >> /root/.zshrc && \
    echo 'setopt HIST_SAVE_NO_DUPS' >> /root/.zshrc && \
    echo 'setopt SHARE_HISTORY' >> /root/.zshrc

# Configure existing ubuntu user for Claude Code compatibility (use existing UID 1000)
RUN usermod --shell /bin/zsh ubuntu && \
    # Give ubuntu sudo access
    echo 'ubuntu ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers && \
    # Add ubuntu user to docker group for Docker socket access
    groupadd -f docker && \
    usermod -aG docker ubuntu

# Switch to ubuntu user and set up user-specific configurations
USER ubuntu

# Install Oh My Zsh for ubuntu user and configure shell
RUN sh -c "$(curl -fsSL https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)" "" --unattended && \
    mkdir -p /home/ubuntu/.shell_history /home/ubuntu/.config && \
    # Install Claude Code for ubuntu user
    curl -fsSL https://claude.ai/install.sh | bash && \
    # Add PATH configuration
    echo 'export PATH="$HOME/.local/bin:$HOME/bin:$PATH"' >> /home/ubuntu/.zshrc && \
    # Add shell history configuration
    echo 'export HISTFILE=/home/ubuntu/.shell_history/.zsh_history' >> /home/ubuntu/.zshrc && \
    echo 'export HISTSIZE=10000' >> /home/ubuntu/.zshrc && \
    echo 'export SAVEHIST=10000' >> /home/ubuntu/.zshrc && \
    echo 'setopt HIST_IGNORE_DUPS' >> /home/ubuntu/.zshrc && \
    echo 'setopt HIST_IGNORE_ALL_DUPS' >> /home/ubuntu/.zshrc && \
    echo 'setopt HIST_SAVE_NO_DUPS' >> /home/ubuntu/.zshrc && \
    echo 'setopt SHARE_HISTORY' >> /home/ubuntu/.zshrc

# Ensure proper ownership of directories that will be mounted as volumes
USER root
RUN chown -R ubuntu:ubuntu /home/ubuntu/.shell_history /home/ubuntu/.config

# Switch back to ubuntu user
USER ubuntu