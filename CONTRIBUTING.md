# Contributing to Sparty's Spreads

Thank you for your interest in contributing to the Sparty's Spreads project! This document provides guidelines for contributing to the codebase.

## 🚀 Getting Started

### Development Environment Setup

1. **Clone and setup the project** (see README.md for detailed instructions)
2. **Ensure Java 17 is installed and configured**
3. **Verify Android Studio is using the correct JDK**
4. **Run lint checks to ensure everything is working**: `./gradlew lint`

### Before You Start

- Check existing issues to see if your bug/feature is already being worked on
- For major changes, please open an issue first to discuss the approach
- Ensure you understand the app's navigation flow (see `navigation_flow.md`)

## 🔧 Development Workflow

### 1. Branch Management
```bash
# Create feature branch from main
git checkout main
git pull origin main
git checkout -b feature/description-of-feature

# Or for bug fixes
git checkout -b bugfix/description-of-bug
```

### 2. Making Changes

#### Code Style Guidelines
- **Follow Java naming conventions**
  - Classes: PascalCase (`MenuActivity`)
  - Methods/variables: camelCase (`loadMenuItems`)
  - Constants: UPPER_SNAKE_CASE (`MEAL_TIME_KEY`)
- **Use meaningful names** for variables and methods
- **Keep methods focused** - one responsibility per method
- **Add comments** for complex business logic
- **Use string resources** instead of hardcoded strings

#### Android-Specific Guidelines
- **Use ContextCompat** for accessing colors/resources
- **Add contentDescription** for all ImageViews (accessibility)
- **Support both orientations** - test portrait and landscape
- **Handle configuration changes** properly (orientation, screen size)
- **Follow Material Design principles**

### 3. Testing Your Changes

#### Required Checks
```bash
# Run lint checks (must pass)
./gradlew lint

# Run unit tests
./gradlew test

# Build the project
./gradlew build
```

#### Manual Testing Checklist
- [ ] Test on both portrait and landscape orientations
- [ ] Verify navigation flow works correctly
- [ ] Test on different screen sizes if possible
- [ ] Ensure no hardcoded strings are visible
- [ ] Check that back button behavior is correct
- [ ] Verify Google Maps integration works (if modified)

### 4. Commit Guidelines

#### Commit Message Format
```
type(scope): brief description

Detailed explanation if needed

- List any breaking changes
- Reference issues: Fixes #123
```

#### Commit Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes

#### Examples
```
feat(menu): add dietary filter options for menu items

- Added vegetarian, vegan, and gluten-free filters
- Updated MenuAdapter to support filtering
- Added filter UI to MenuActivity

Fixes #45
```

```
fix(navigation): preserve meal selection during orientation change

The selected meal time was being reset when rotating the device.
Added proper state saving/restoration in MenuActivity.

Fixes #23
```

## 📋 Types of Contributions

### 🐛 Bug Reports
When reporting bugs, please include:
- **Device info**: Android version, device model
- **Steps to reproduce** the issue
- **Expected behavior** vs **actual behavior**
- **Screenshots** if the issue is visual
- **Error logs** if applicable

### ✨ Feature Requests
For new features:
- **Describe the use case** - why is this needed?
- **Provide mockups** if it's a UI change
- **Consider impact** on existing functionality
- **Check if it aligns** with the app's purpose

### 🔧 Code Contributions

#### Areas Needing Help
- **Seating availability integration** (future feature)
- **Real-time menu updates** (API integration)
- **Accessibility improvements** (screen reader support)
- **Performance optimizations** (image loading, database queries)
- **UI/UX enhancements** (animations, better layouts)
- **Testing coverage** (unit tests, UI tests)

#### Pull Request Process
1. **Create feature branch** from `main`
2. **Make changes** following guidelines above
3. **Add/update tests** as appropriate
4. **Run all checks** and ensure they pass
5. **Update documentation** if needed
6. **Create pull request** with clear description
7. **Respond to code review** feedback promptly

#### Pull Request Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Lint checks pass
- [ ] Unit tests pass
- [ ] Manual testing completed
- [ ] Tested on multiple orientations

## Screenshots (if UI changes)
Include before/after screenshots

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No hardcoded strings added
```

## 🔍 Code Review Guidelines

### For Reviewers
- **Be constructive** and specific in feedback
- **Focus on code quality**, not personal preferences
- **Check for**: functionality, readability, performance, security
- **Verify tests** cover the changes
- **Ensure documentation** is updated if needed

### For Contributors
- **Respond promptly** to review feedback
- **Be open to suggestions** and learning opportunities
- **Ask questions** if feedback isn't clear
- **Update PR** based on feedback before re-requesting review

## 🏗️ Architecture Considerations

### When Adding New Features
- **Consider existing patterns** - follow established conventions
- **Database changes** - update schema carefully, consider migrations
- **Navigation** - ensure it fits the existing flow
- **State management** - handle configuration changes properly
- **Resource management** - use appropriate lifecycle methods

### Performance Guidelines
- **Avoid memory leaks** - properly manage contexts and listeners
- **Optimize layouts** - minimize view hierarchy depth
- **Efficient database queries** - use appropriate indexes
- **Image handling** - use appropriate scaling and caching

## 📞 Getting Help

### Communication Channels
- **Issues**: For bugs and feature requests
- **Pull Request Comments**: For code-specific discussions
- **Email**: Contact repository maintainers directly

### Resources
- [Android Developer Documentation](https://developer.android.com/)
- [Material Design Guidelines](https://material.io/design)
- [Git Best Practices](https://git-scm.com/doc)

## 🎯 Project Goals

Keep these in mind when contributing:
- **Student-focused**: Easy for MSU students to use
- **Reliable**: Accurate menu and nutrition information
- **Accessible**: Works for users with disabilities
- **Performant**: Fast and responsive on various devices
- **Maintainable**: Clean, well-documented code

## ⚖️ Code of Conduct

- **Be respectful** in all interactions
- **Focus on constructive** feedback and discussions
- **Help others learn** - explain reasoning behind suggestions
- **Assume good intentions** from all contributors

Thank you for contributing to Sparty's Spreads! 🎉