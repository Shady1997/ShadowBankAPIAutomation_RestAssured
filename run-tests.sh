#!/bin/bash

# Banking API Test Execution Script
# This script provides various options to run tests with different configurations

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
ENVIRONMENT="test"
SUITE="testng.xml"
PARALLEL=""
THREAD_COUNT=""
RETRY_COUNT="1"
LOGGING="true"
CLEAN="true"
REPORT_ONLY="false"

# Function to display help
show_help() {
    echo "Banking API Test Execution Script"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -e, --env ENVIRONMENT     Set environment (test, staging, production) [default: test]"
    echo "  -s, --suite SUITE         Test suite to run [default: testng.xml]"
    echo "                           Options: testng.xml, user-api-suite.xml, account-api-suite.xml,"
    echo "                                   transaction-api-suite.xml, e2e-suite.xml, smoke-suite.xml,"
    echo "                                   regression-suite.xml"
    echo "  -p, --parallel TYPE       Enable parallel execution (methods, classes, tests)"
    echo "  -t, --threads COUNT       Number of parallel threads [default: 3]"
    echo "  -r, --retry COUNT         Number of retries for failed tests [default: 2]"
    echo "  -l, --logging BOOL        Enable detailed logging [default: true]"
    echo "  --no-clean               Skip clean before test execution"
    echo "  --report-only            Only generate reports, skip test execution"
    echo "  --allure-serve           Generate and serve Allure report"
    echo "  -h, --help               Display this help message"
    echo ""
    echo "Examples:"
    echo "  $0                                          # Run all tests with default settings"
    echo "  $0 -e staging -s smoke-suite.xml           # Run smoke tests on staging"
    echo "  $0 -p methods -t 5                         # Run with 5 parallel threads"
    echo "  $0 -s user-api-suite.xml --no-clean        # Run user API tests without clean"
    echo "  $0 --report-only --allure-serve             # Generate and serve reports"
}

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to validate environment
validate_environment() {
    case $ENVIRONMENT in
        test|staging|production)
            print_info "Using environment: $ENVIRONMENT"
            ;;
        *)
            print_error "Invalid environment: $ENVIRONMENT"
            print_info "Valid environments: test, staging, production"
            exit 1
            ;;
    esac
}

# Function to validate test suite
validate_suite() {
    if [[ ! -f "$SUITE" ]]; then
        print_error "Test suite file not found: $SUITE"
        exit 1
    fi
    print_info "Using test suite: $SUITE"
}

# Function to run maven tests
run_tests() {
    print_info "Starting test execution..."

    # Build maven command
    MVN_CMD="mvn"

    if [[ "$CLEAN" == "true" ]]; then
        MVN_CMD="$MVN_CMD clean"
    fi

    MVN_CMD="$MVN_CMD test"

    # Add environment
    MVN_CMD="$MVN_CMD -Denv=$ENVIRONMENT"

    # Add test suite
    MVN_CMD="$MVN_CMD -DsuiteXmlFile=$SUITE"

    # Add parallel execution
    if [[ ! -z "$PARALLEL" ]]; then
        MVN_CMD="$MVN_CMD -Dparallel=$PARALLEL"
        if [[ ! -z "$THREAD_COUNT" ]]; then
            MVN_CMD="$MVN_CMD -DthreadCount=$THREAD_COUNT"
        fi
    fi

    # Add retry count
    MVN_CMD="$MVN_CMD -Dretry.count=$RETRY_COUNT"

    # Add logging
    MVN_CMD="$MVN_CMD -Dlogging.enabled=$LOGGING"

    print_info "Executing: $MVN_CMD"

    # Execute the command
    eval $MVN_CMD

    if [[ $? -eq 0 ]]; then
        print_success "Tests completed successfully!"
    else
        print_error "Tests failed or completed with errors"
        return 1
    fi
}

# Function to generate Allure report
generate_allure_report() {
    print_info "Generating Allure report..."

    if command -v allure &> /dev/null; then
        mvn allure:report
        if [[ $? -eq 0 ]]; then
            print_success "Allure report generated successfully!"
            print_info "Report location: target/site/allure-maven-plugin/"
        else
            print_error "Failed to generate Allure report"
        fi
    else
        print_warning "Allure command line tool not found. Installing via Maven..."
        mvn allure:serve
    fi
}

# Function to serve Allure report
serve_allure_report() {
    print_info "Starting Allure report server..."
    mvn allure:serve
}

# Function to display test summary
display_summary() {
    print_info "=== Test Execution Summary ==="
    print_info "Environment: $ENVIRONMENT"
    print_info "Test Suite: $SUITE"
    print_info "Parallel Execution: ${PARALLEL:-disabled}"
    print_info "Thread Count: ${THREAD_COUNT:-default}"
    print_info "Retry Count: $RETRY_COUNT"
    print_info "Logging Enabled: $LOGGING"
    print_info "================================"

    # Display report locations
    echo ""
    print_info "Generated Reports:"
    print_info "- ExtentReports: test-output/"
    print_info "- Allure Reports: target/allure-results/"
    print_info "- Test Logs: logs/"
    print_info "- Surefire Reports: target/surefire-reports/"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -e|--env)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -s|--suite)
            SUITE="$2"
            shift 2
            ;;
        -p|--parallel)
            PARALLEL="$2"
            shift 2
            ;;
        -t|--threads)
            THREAD_COUNT="$2"
            shift 2
            ;;
        -r|--retry)
            RETRY_COUNT="$2"
            shift 2
            ;;
        -l|--logging)
            LOGGING="$2"
            shift 2
            ;;
        --no-clean)
            CLEAN="false"
            shift
            ;;
        --report-only)
            REPORT_ONLY="true"
            shift
            ;;
        --allure-serve)
            ALLURE_SERVE="true"
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Main execution
print_info "Banking API Test Automation Framework"
print_info "======================================"

# Validate inputs
validate_environment
validate_suite

# Execute based on options
if [[ "$REPORT_ONLY" == "true" ]]; then
    print_info "Report-only mode enabled. Skipping test execution."
    generate_allure_report
    if [[ "$ALLURE_SERVE" == "true" ]]; then
        serve_allure_report
    fi
else
    # Run tests
    if run_tests; then
        # Generate reports after successful test execution
        generate_allure_report

        # Serve Allure report if requested
        if [[ "$ALLURE_SERVE" == "true" ]]; then
            serve_allure_report
        fi

        display_summary
        exit 0
    else
        print_error "Test execution failed"
        display_summary
        exit 1
    fi
fi